package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

public class SubmitDnCaseTest extends CcdSubmissionSupport {
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";

    @Value("${case.orchestration.maintenance.submit-dn.context-path}")
    private String contextPath;

    @Test
    public void givenUserTokenIsNull_whenSubmitDn_thenReturnBadRequest() throws Exception {
        Response cosResponse = submitDnCase(null, 1L, "dn-submit.json", contextPath);

        assertEquals(BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenNoCaseData_whenSubmitDn_thenReturnBadRequest() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        Response cosResponse = submitDnCase(userDetails.getAuthToken(), 1L, null, contextPath);

        assertEquals(BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void whenSubmitDn_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);
        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, "aosSubmittedUndefended", userDetails);

        Response cosResponse = submitDnCase(userDetails.getAuthToken(), caseDetails.getId(),
            "dn-submit.json", contextPath);

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        assertEquals("AwaitingLegalAdvisorReferral", cosResponse.path("state"));
    }
}
