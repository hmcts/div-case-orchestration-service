package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;

public class SubmitDnCaseTest extends CcdSubmissionSupport {
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String AOS_SUBMITTED_UNDEFENDED_EVENT_ID = "aosSubmittedUndefended";
    private static final String REFER_TO_LEGAL_ADVISOR_EVENT_ID = "refertoLegalAdvisor";
    private static final String DN_CLARIFICATION_REQUESTED_EVENT_ID = "dnClarificationRequested";

    private UserDetails userDetails;
    private CaseDetails caseDetails;

    @Before
    public void setup() {
        userDetails = createCitizenUser();
        caseDetails = submitCase("submit-complete-case.json", userDetails);
    }

    @Test
    public void whenSubmitDn_thenProceedAsExpected() throws Exception {
        updateCaseForCitizen(TEST_AOS_STARTED_EVENT_ID);
        updateCaseForCitizen(AOS_SUBMITTED_UNDEFENDED_EVENT_ID);

        Response cosResponse = submitDnCase();

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        assertEquals("AwaitingLegalAdvisorReferral", cosResponse.path("state"));
    }

    @Test
    public void whenSubmitDnWithAwaitingClarification_thenProceedAsExpected() throws Exception {
        updateCaseForCitizen(TEST_AOS_STARTED_EVENT_ID);
        updateCaseForCitizen(AOS_SUBMITTED_UNDEFENDED_EVENT_ID);
        submitDnCase();

        // Move to AwaitingClarification
        updateCaseForCaseworker(REFER_TO_LEGAL_ADVISOR_EVENT_ID);
        updateCaseForCaseworker(DN_CLARIFICATION_REQUESTED_EVENT_ID);

        Response cosResponse = submitDnCase();

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        assertEquals("ClarificationSubmitted", cosResponse.path("state"));
    }

    private void updateCaseForCitizen(final String eventId) {
        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, eventId, userDetails);
    }

    private void updateCaseForCaseworker(final String eventId) {
        updateCase(String.valueOf(caseDetails.getId()), null, eventId);
    }

    private Response submitDnCase() throws Exception {
        return submitDnCase(
            userDetails.getAuthToken(),
            caseDetails.getId(),
            "dn-submit.json"
        );
    }
}
