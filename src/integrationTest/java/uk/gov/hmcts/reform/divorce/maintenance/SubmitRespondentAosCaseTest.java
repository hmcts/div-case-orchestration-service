package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.util.DateConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

public class SubmitRespondentAosCaseTest extends CcdSubmissionSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit-aos/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String ID = "id";
    private static final String STATE = "state";
    private static final String AOS_AWAITING_SOL = "AosAwaitingSol";
    private static final String AOS_DEFEND_CONSENT_JSON = "aos-defend-consent.json";

    private static final String AOS_SOLICITOR_REPRESENTATION_JSON = "aos-solicitor-representation.json";

    private static final String SUBMIT_COMPLETE_CASE_JSON = "submit-unlinked-case.json";

    @Test
    public void givenConsentAndDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON, userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()),
            null,
            TEST_AOS_STARTED_EVENT_ID,
            userDetails);

        Response cosResponse = submitRespondentAosCase(userDetails.getAuthToken(), caseDetails.getId(),
            loadJson(PAYLOAD_CONTEXT_PATH + AOS_DEFEND_CONSENT_JSON));

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path(ID));
        assertEquals(AOS_SUBMITTED_AWAITING_ANSWER, cosResponse.path(STATE));
        assertAosSubmittedData(userDetails, caseDetails.getId().toString());
    }

    @Test
    public void givenRespondentSolicitorRepresented_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON, userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()),
                null,
                TEST_AOS_STARTED_EVENT_ID,
                userDetails);

        Response cosResponse = submitRespondentAosCase(userDetails.getAuthToken(), caseDetails.getId(),
                loadJson(PAYLOAD_CONTEXT_PATH + AOS_SOLICITOR_REPRESENTATION_JSON));

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path(ID));
        assertEquals(AOS_AWAITING_SOL, cosResponse.path(STATE));
        assertNull(caseDetails.getData().get(RECEIVED_AOS_FROM_RESP));
        assertNull(caseDetails.getData().get(RECEIVED_AOS_FROM_RESP_DATE));
    }

    private void assertAosSubmittedData(UserDetails userDetails, String caseId) {
        CaseDetails caseDetails = this.retrieveCase(userDetails, caseId);
        assertEquals(YES_VALUE, caseDetails.getData().get(RECEIVED_AOS_FROM_RESP));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern(CCD_DATE_FORMAT)), caseDetails.getData().get(RECEIVED_AOS_FROM_RESP_DATE));
    }
}
