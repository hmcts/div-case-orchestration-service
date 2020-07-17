package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aos;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static java.util.Arrays.asList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport.buildDateForTodayMinusGivenPeriod;

public class AosOverdueTest extends MockedFunctionalTest {

    private static final String TIME_LIMIT_FOR_AOS = "30d";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldMoveEligibleCasesToAosOverdue() throws Exception {
        QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AOS_AWAITING);
        QueryBuilder dateFilter = QueryBuilders.rangeQuery("data." + CCD_DUE_DATE).lte(buildDateForTodayMinusGivenPeriod(TIME_LIMIT_FOR_AOS));
        String expectedElasticSearchQuery = stubCaseMaintenanceSearchEndpoint(asList(
            CaseDetails.builder().caseId("123").build(),
            CaseDetails.builder().caseId("456").build()
        ), stateQuery, dateFilter);

        mockMvc.perform(post("/cases/aos/make-overdue").header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk());

        maintenanceServiceServer.verify(RequestPatternBuilder.newRequestPattern().withUrl(CASE_MAINTENANCE_CLIENT_SEARCH_URL)
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withRequestBody(equalTo(expectedElasticSearchQuery))
        );
    }

}