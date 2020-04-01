package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_LEGAL_ADVISOR_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;

public class SubmitDnCaseTest extends CcdSubmissionSupport {
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String AOS_SUBMITTED_UNDEFENDED_EVENT_ID = "aosSubmittedUndefended";

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
        assertEquals(AWAITING_LEGAL_ADVISOR_REFERRAL, cosResponse.path(CASE_STATE_JSON_KEY));
    }

    @Ignore // Needs config to pass
    @Test
    public void whenSubmitDnWithAwaitingClarification_thenProceedAsExpected() throws Exception {
        updateCaseForCitizen(TEST_AOS_STARTED_EVENT_ID);
        updateCaseForCitizen(AOS_SUBMITTED_UNDEFENDED_EVENT_ID);
        submitDnCase();

        // Move to AwaitingClarification
        updateCaseForCaseworker("refertoLegalAdvisor");
        updateCaseForCaseworker("dnClarificationRequested");

        Response cosResponse = submitDnCase();

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        assertEquals("ClarificationSubmitted", cosResponse.path(CASE_STATE_JSON_KEY));
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
