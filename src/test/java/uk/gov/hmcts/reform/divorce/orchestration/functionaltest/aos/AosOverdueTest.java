package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aos;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.AOS_NOT_RECEIVED_FOR_ALTERNATIVE_METHOD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.AOS_NOT_RECEIVED_FOR_BAILIFF_APPLICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.AOS_NOT_RECEIVED_FOR_PROCESS_SERVER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.NOT_RECEIVED_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.BAILIFF_SERVICE_SUCCESSFUL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_ALTERNATIVE_METHOD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_PROCESS_SERVER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_DRAFTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchSupport.buildDateForTodayMinusGivenPeriod;

@TestPropertySource(properties = {
    "AOS_OVERDUE_GRACE_PERIOD=0"
})
public class AosOverdueTest extends MockedFunctionalTest {

    private static final String TEST_CONFIGURED_AOS_OVERDUE_GRACE_PERIOD = "0d";

    @MockBean
    private AuthUtil authUtil;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);

        stubUpdateCaseEndpointForGivenEvents(NOT_RECEIVED_AOS,
            AOS_NOT_RECEIVED_FOR_PROCESS_SERVER,
            AOS_NOT_RECEIVED_FOR_ALTERNATIVE_METHOD,
            AOS_NOT_RECEIVED_FOR_BAILIFF_APPLICATION);
    }

    @After
    public void tearDown() {
        maintenanceServiceServer.resetAll();
    }

    @Test
    public void shouldMoveEligibleCasesToAosOverdue() throws Exception {
        QueryBuilder expectedQuery = buildCaseMaintenanceQuery();
        stubCaseMaintenanceSearchEndpoint(asList(
            CaseDetails.builder().state(AOS_STARTED).caseId("1").build(),
            CaseDetails.builder().state(AOS_AWAITING).caseId("2").build(),
            CaseDetails.builder().state(AOS_AWAITING).caseId("3").build(),
            CaseDetails.builder().state(AOS_STARTED).caseId("4").caseData(Map.of(SERVED_BY_PROCESS_SERVER, YES_VALUE)).build(),
            CaseDetails.builder().state(AOS_AWAITING).caseId("5").caseData(Map.of(SERVED_BY_PROCESS_SERVER, YES_VALUE)).build(),
            CaseDetails.builder().state(AOS_STARTED).caseId("6").caseData(Map.of(SERVED_BY_ALTERNATIVE_METHOD, YES_VALUE)).build(),
            CaseDetails.builder().state(AOS_AWAITING).caseId("7").caseData(Map.of(SERVED_BY_ALTERNATIVE_METHOD, YES_VALUE)).build(),
            CaseDetails.builder().state(AOS_AWAITING).caseId("8").caseData(Map.of(BAILIFF_SERVICE_SUCCESSFUL, YES_VALUE)).build()
        ), expectedQuery);

        mockMvc.perform(post("/cases/aos/make-overdue").header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk());

        await().untilAsserted(() -> {
            verifyCaseWasUpdated("2", NOT_RECEIVED_AOS);
            verifyCaseWasUpdated("3", NOT_RECEIVED_AOS);
            verifyCaseWasUpdated("4", AOS_NOT_RECEIVED_FOR_PROCESS_SERVER);
            verifyCaseWasUpdated("5", AOS_NOT_RECEIVED_FOR_PROCESS_SERVER);
            verifyCaseWasUpdated("6", AOS_NOT_RECEIVED_FOR_ALTERNATIVE_METHOD);
            verifyCaseWasUpdated("7", AOS_NOT_RECEIVED_FOR_ALTERNATIVE_METHOD);
            verifyCaseWasUpdated("8", AOS_NOT_RECEIVED_FOR_BAILIFF_APPLICATION);
        });
        verifyCaseWasNotUpdated("1", NOT_RECEIVED_AOS);
    }

    @Test
    public void shouldMoveEligibleAosDraftedCasesToAosOverdue() throws Exception {
        QueryBuilder expectedCMSQuery = buildCaseMaintenanceQuery();
        List<CaseDetails> expectedCMSResponse = buildCaseMaintenanceResponseWithAosDraftedCases();
        stubCaseMaintenanceSearchEndpoint(expectedCMSResponse, expectedCMSQuery);

        mockMvc.perform(post("/cases/aos/make-overdue").header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk());

        await().untilAsserted(() -> {
            verifyCaseWasUpdated("2", NOT_RECEIVED_AOS);
            verifyCaseWasUpdated("3", AOS_NOT_RECEIVED_FOR_PROCESS_SERVER);
            verifyCaseWasUpdated("4", NOT_RECEIVED_AOS);
            verifyCaseWasUpdated("5", AOS_NOT_RECEIVED_FOR_ALTERNATIVE_METHOD);
        });
        verifyCaseWasNotUpdated("1", NOT_RECEIVED_AOS);
    }

    private List<CaseDetails> buildCaseMaintenanceResponseWithAosDraftedCases() {
        return asList(
            CaseDetails.builder().state(AOS_STARTED).caseId("1").build(),
            CaseDetails.builder().state(AOS_DRAFTED).caseId("2").build(),
            CaseDetails.builder().state(AOS_DRAFTED).caseId("3").caseData(Map.of(SERVED_BY_PROCESS_SERVER, YES_VALUE)).build(),
            CaseDetails.builder().state(AOS_AWAITING).caseId("4").build(),
            CaseDetails.builder().state(AOS_DRAFTED).caseId("5").caseData(Map.of(SERVED_BY_ALTERNATIVE_METHOD, YES_VALUE)).build()
        );
    }

    private BoolQueryBuilder buildCaseMaintenanceQuery() {
        return QueryBuilders.boolQuery()
            .filter(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, String.join(SPACE, AOS_AWAITING, AOS_STARTED, AOS_DRAFTED))
                .operator(Operator.OR))
            .filter(QueryBuilders.rangeQuery("data." + DUE_DATE)
                .lt(buildDateForTodayMinusGivenPeriod(TEST_CONFIGURED_AOS_OVERDUE_GRACE_PERIOD))
            );
    }

    private void verifyCaseWasUpdated(String caseId, String expectedEvent) {
        verifyCaseWasMovedGivenNumberOfTimes(caseId, expectedEvent, 1);
    }

    private void verifyCaseWasNotUpdated(String caseId, String expectedEvent) {
        verifyCaseWasMovedGivenNumberOfTimes(caseId, expectedEvent, 0);
    }

    private void verifyCaseWasMovedGivenNumberOfTimes(String caseId, String expectedEvent, int expectedRequestCount) {
        //Checks specific event was called
        maintenanceServiceServer.verify(expectedRequestCount, newRequestPattern()
            .withUrl(format("/casemaintenance/version/1/updateCase/%s/%s", caseId, expectedEvent))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withRequestBody(equalToJson("{}"))
        );

        //Checks no other event was called
        maintenanceServiceServer.verify(expectedRequestCount,
            newRequestPattern(RequestMethod.POST, urlPathMatching(format("/casemaintenance/version/1/updateCase/%s/.*", caseId)))
                .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                .withRequestBody(new AnythingPattern())
        );
    }

    private void stubUpdateCaseEndpointForGivenEvents(String... eventIds) {
        Arrays.stream(eventIds).forEach(
            eventId -> maintenanceServiceServer.stubFor(
                WireMock.post(urlPathMatching("/casemaintenance/version/1/updateCase/.*/" + eventId))
                    .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                    .willReturn(ok()))
        );
    }

}