package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_SUBMIT_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY;

public class GetPetitionIssueFeesTest extends IntegrationTest {

    @Value("${case.orchestration.solicitor.petition-issue-fees.context-path}")
    private String contextPath;

    @Autowired
    private CcdClientSupport ccdClientSupport;

    private static final String BASE_CASE_RESPONSE = "fixtures/solicitor/case-data.json";

    private UserDetails solicitorUser;

    @Before
    public void setUp() {
        solicitorUser = createSolicitorUser();
    }

    @Test
    public void givenCallbackRequest_whenGetPetitionIssueFees_thenReturnUpdatedData() {
        final Map<String, Object> caseData = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = ccdClientSupport.submitSolicitorCase(caseData, solicitorUser);

        final CcdCallbackRequest callbackData = CcdCallbackRequest.builder()
            .eventId(SOLICITOR_SUBMIT_EVENT)
            .caseDetails(CaseDetails.builder()
                .caseId(String.valueOf(caseDetails.getId()))
                .state(caseDetails.getState())
                .caseData(caseDetails.getData())
                .build())
            .build();

        Response response = RestUtil.postToRestService(
            serverUrl + contextPath,
            getRequestHeaders(),
            ObjectMapperTestUtil.convertObjectToJsonString(callbackData)
        );

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        Map<String, Object> responseData = response.getBody().path(DATA);
        OrderSummary orderSummary = ObjectMapperTestUtil.convertObject(responseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class);
        assertThat(orderSummary, is(notNullValue()));
        assertThat(responseData, hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, orderSummary.getPaymentTotalInPounds()));
    }

    @Test
    public void givenAmendCaseCallbackRequest_whenGetPetitionIssueFees_thenReturnUpdatedData() {
        final Map<String, Object> caseData = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        caseData.put(PREVIOUS_CASE_ID_CCD_KEY, new CaseLink("1234567890123456"));
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = ccdClientSupport.submitSolicitorCase(caseData, solicitorUser);

        final CcdCallbackRequest callbackData = CcdCallbackRequest.builder()
            .eventId(SOLICITOR_SUBMIT_EVENT)
            .caseDetails(CaseDetails.builder()
                .caseId(String.valueOf(caseDetails.getId()))
                .state(caseDetails.getState())
                .caseData(caseDetails.getData())
                .build())
            .build();

        Response response = RestUtil.postToRestService(
            serverUrl + contextPath,
            getRequestHeaders(),
            ObjectMapperTestUtil.convertObjectToJsonString(callbackData)
        );

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        Map<String, Object> responseData = response.getBody().path(DATA);
        OrderSummary orderSummary = ObjectMapperTestUtil.convertObject(responseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class);
        assertThat(orderSummary, is(notNullValue()));
        assertThat(responseData, hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, orderSummary.getPaymentTotalInPounds()));
    }

    private Map<String, Object> getRequestHeaders() {
        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        requestHeaders.put(HttpHeaders.AUTHORIZATION, solicitorUser.getAuthToken());
        return requestHeaders;
    }

}