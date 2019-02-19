package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

public class SubmitAosCaseTest extends CcdSubmissionSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit-aos/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String ID = "id";
    private static final String STATE = "state";
    private static final String AOS_SUBMITTED_AWAITING_ANSWER = "AosSubmittedAwaitingAnswer";
    private static final String AWAITING_DECREE_NISI = "AwaitingDecreeNisi";
    private static final String AOS_COMPLETED = "AosCompleted";

    private static final String AOS_DEFEND_CONSENT_JSON = "aos-defend-consent.json";
    private static final String AOS_DEFEND_NO_CONSENT_JSON = "aos-defend-no-consent.json";
    private static final String AOS_NO_DEFEND_CONSENT_JSON = "aos-no-defend-consent.json";
    private static final String AOS_NO_DEFEND_NO_CONSENT_JSON = "aos-no-defend-no-consent.json";

    private static final String SUBMIT_COMPLETE_CASE_JSON = "submit-complete-case.json";
    private static final String SUBMIT_COMPLETE_CASE_REASON_ADULTERY_JSON = "submit-complete-case-reason-adultery.json";
    private static final String SUBMIT_COMPLETE_CASE_REASON_2_YEAR_SEP_JSON = "submit-complete-case-reason-2yearSep.json";



    @Test
    public void givenUserTokenIsNull_whenSubmitAos_thenReturnBadRequest() throws Exception {
        Response cosResponse = submitAosCase(
                null,
                1L,
                loadJson(PAYLOAD_CONTEXT_PATH + AOS_DEFEND_CONSENT_JSON));

        assertEquals(BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenNoCaseData_whenSubmitAos_thenReturnBadRequest() {
        final UserDetails userDetails = createCitizenUser();

        Response cosResponse = submitAosCase(userDetails.getAuthToken(),
                1L,
                null);

        assertEquals(BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenConsentAndDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON, userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()),
                null,
                TEST_AOS_STARTED_EVENT_ID,
                userDetails);

        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(),
                loadJson(PAYLOAD_CONTEXT_PATH + AOS_DEFEND_CONSENT_JSON));

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path(ID));
        assertEquals(AOS_SUBMITTED_AWAITING_ANSWER, cosResponse.path(STATE));
    }

    @Test
    public void givenNoConsentAndDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON, userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()),
                null,
                TEST_AOS_STARTED_EVENT_ID,
                userDetails);

        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(),
                loadJson(PAYLOAD_CONTEXT_PATH + AOS_DEFEND_NO_CONSENT_JSON));

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path(ID));
        assertEquals(AOS_SUBMITTED_AWAITING_ANSWER, cosResponse.path(STATE));
    }

    @Test
    public void givenConsentAndNoDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON, userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()),
                null,
                TEST_AOS_STARTED_EVENT_ID,
                userDetails);

        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(),
                loadJson(PAYLOAD_CONTEXT_PATH + AOS_NO_DEFEND_CONSENT_JSON));

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path(ID));
        assertEquals(AWAITING_DECREE_NISI, cosResponse.path(STATE));
    }

    @Test
    public void givenNoConsentAndNoDefendAndReasonIsNotAdultery_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON, userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()),
                null,
                TEST_AOS_STARTED_EVENT_ID,
                userDetails);

        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(),
                loadJson(PAYLOAD_CONTEXT_PATH + AOS_NO_DEFEND_NO_CONSENT_JSON));

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path(ID));
        assertEquals(AWAITING_DECREE_NISI, cosResponse.path(STATE));
    }

    @Test
    public void givenNoConsentAndNoDefendAndReasonIsAdultery_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_REASON_ADULTERY_JSON, userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()),
                null,
                TEST_AOS_STARTED_EVENT_ID,
                userDetails);

        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(),
                loadJson(PAYLOAD_CONTEXT_PATH + AOS_NO_DEFEND_NO_CONSENT_JSON));

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path(ID));
        assertEquals(AOS_COMPLETED, cosResponse.path(STATE));
    }

    @Test
    public void givenNoConsentAndNoDefendAndReasonIs2YearSeparation_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_REASON_2_YEAR_SEP_JSON, userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()),
                null,
                TEST_AOS_STARTED_EVENT_ID,
                userDetails);

        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(),
                loadJson(PAYLOAD_CONTEXT_PATH + AOS_NO_DEFEND_NO_CONSENT_JSON));

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path(ID));
        assertEquals(AOS_COMPLETED, cosResponse.path(STATE));
    }
}