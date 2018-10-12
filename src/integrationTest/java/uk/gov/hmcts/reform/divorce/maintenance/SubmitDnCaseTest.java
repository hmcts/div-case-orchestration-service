package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

public class SubmitDnCaseTest extends CcdSubmissionSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit-dn/";
    private static final String TEST_DN_STARTED_EVENT_ID = "AwaitingLegalAdvisorReferral";

    @Value("${case.orchestration.maintenance.submit-dn.context-path}")
    private String contextPath;

    @Test
    public void givenUserTokenIsNull_whenSubmitAos_thenReturnBadRequest() throws Exception {
        Response cosResponse = submitDnCase(null, 1L, "dn-submit.json");

        assertEquals(BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenNoCaseData_whenSubmitAos_thenReturnBadRequest() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        Response cosResponse = submitDnCase(userDetails.getAuthToken(), 1L, null);

        assertEquals(BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void whenSubmitDn_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_DN_STARTED_EVENT_ID, userDetails);

        Response cosResponse = submitDnCase(userDetails.getAuthToken(), caseDetails.getId(),
            "dn-submit.json");

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        assertEquals("AwaitingLegalAdvisorReferral", cosResponse.path("state"));
    }



    private Response submitDnCase(String userToken, Long caseId, String filePath) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.postToRestService(
            serverUrl + contextPath + "/" + caseId,
            headers,
            filePath == null ? null : loadJson(PAYLOAD_CONTEXT_PATH + filePath)
        );
    }
}
