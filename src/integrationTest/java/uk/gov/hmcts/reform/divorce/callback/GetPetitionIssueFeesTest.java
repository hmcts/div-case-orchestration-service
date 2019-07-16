package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_SUBMIT_EVENT;

public class GetPetitionIssueFeesTest extends IntegrationTest {

    private static final String DATA_KEY = "data";
    private static final String ERRORS_KEY = "errors";

    @Value("${case.orchestration.solicitor.petition-issue-fees.context-path}")
    private String contextPath;

    @Autowired
    private CcdClientSupport ccdClientSupport;

    private static final String BASE_CASE_RESPONSE = "fixtures/solicitor/case-data.json";

    @Test
    public void givenCallbackRequest_whenGetPetitionIssueFees_thenReturnUpdatedData() {
        final Map<String, Object> caseData = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        final UserDetails solicitorUser = createSolicitorUser();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = ccdClientSupport.submitCaseForSolicitor(caseData, solicitorUser);

        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, solicitorUser.getAuthToken());

        final CcdCallbackRequest callbackData = CcdCallbackRequest.builder()
            .eventId(SOLICITOR_SUBMIT_EVENT)
            .caseDetails(CaseDetails.builder()
                .caseId(String.valueOf(caseDetails.getId()))
                .state(caseDetails.getState())
                .caseData(caseData)
                .build())
            .build();

        Response response = RestUtil.postToRestService(
            serverUrl + contextPath,
            headers,
            ObjectMapperTestUtil.convertObjectToJsonString(callbackData)
        );

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Map<String, Object> responseData = response.getBody().path(DATA_KEY);

        assertNotNull(responseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY));
    }

    @Test
    public void givenUnauthorizedRequest_whenGetPetitionIssueFees_thenReturnErrorData() {
        final Map<String, Object> caseData = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        final UserDetails solicitorUser = createSolicitorUser();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = ccdClientSupport.submitCaseForSolicitor(caseData, solicitorUser);

        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, AUTHORIZATION_CODE);

        final CcdCallbackRequest callbackData = CcdCallbackRequest.builder()
            .eventId(SOLICITOR_SUBMIT_EVENT)
            .caseDetails(CaseDetails.builder()
                .caseId(String.valueOf(caseDetails.getId()))
                .state(caseDetails.getState())
                .caseData(caseData)
                .build())
            .build();

        Response response = RestUtil.postToRestService(
            serverUrl + contextPath,
            headers,
            ObjectMapperTestUtil.convertObjectToJsonString(callbackData)
        );

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        ArrayList<String> responseData = response.getBody().path(ERRORS_KEY);

        assertEquals(responseData.size(), 1);
    }

}
