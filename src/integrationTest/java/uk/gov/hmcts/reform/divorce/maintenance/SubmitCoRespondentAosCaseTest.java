package uk.gov.hmcts.reform.divorce.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.RetrieveAosCaseSupport;

import java.time.LocalDate;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DEFENDED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOT_RECEIVED_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.util.DateConstants.CCD_DATE_FORMATTER;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

@Slf4j
@SuppressWarnings("Duplicates")
public class SubmitCoRespondentAosCaseTest extends RetrieveAosCaseSupport {

    private static final String CO_RESPONDENT_PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/co-respondent/";
    private static final String RESPONDENT_PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit-aos/";
    private static final String TEST_AOS_AWAITING_EVENT_ID = "testAosAwaiting";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String TEST_AWAITING_DECREE_ABSOLUTE = "testAwaitingDecreeAbsolute";
    private static final String SUBMIT_COMPLETE_CASE_JSON_FILE_PATH = "submit-complete-case.json";
    private static final String AOS_DEFEND_CONSENT_JSON_FILE_PATH = "aos-defend-consent.json";
    private static final String CO_RESP_ANSWERS_JSON_FILE_PATH = "co-respondent-answers.json";
    private static final String CO_RESP_DEFENDED_ANSWERS_JSON_FILE_PATH = "co-respondent-defended-answers.json";

    private static final String STATE_KEY = "state";
    private static final String ID = "id";

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenUserTokenIsNull_whenSubmitCoRespondentAos_thenReturnBadRequest() throws Exception {
        final String coRespondentAnswersJson = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_ANSWERS_JSON_FILE_PATH);
        final UserDetails user = UserDetails.builder()
            .emailAddress(CO_RESP_EMAIL_ADDRESS)
            .build();
        Response cosResponse = submitCoRespondentAosCase(user, coRespondentAnswersJson);

        assertEquals(BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenNoCaseData_whenSubmitAos_thenReturnBadRequest() {
        final UserDetails userDetails = createCitizenUser();

        Response coRespondentSubmissionResponse = submitCoRespondentAosCase(userDetails, null);

        assertThat(coRespondentSubmissionResponse.getStatusCode(), is(BAD_REQUEST.value()));
    }

    @Test
    public void givenCaseIsDisallowedState_whenSubmittingCoRespondentAnswers_thenReturnBadRequest() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));
        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AWAITING_DECREE_ABSOLUTE, userDetails);

        final String coRespondentAnswersJson = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_ANSWERS_JSON_FILE_PATH);
        Response coRespondentSubmissionResponse = submitCoRespondentAosCase(userDetails, coRespondentAnswersJson);

        assertThat(coRespondentSubmissionResponse.getStatusCode(), is(BAD_REQUEST.value()));
    }

    @Test
    public void canSubmitAndRetrieveCoRespondentAnswers() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));
        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_AWAITING_EVENT_ID, userDetails);

        final String coRespondentAnswersJson = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_ANSWERS_JSON_FILE_PATH);
        final Response coRespondentSubmissionResponse = submitCoRespondentAosCase(userDetails, coRespondentAnswersJson);

        assertThat(coRespondentSubmissionResponse.getStatusCode(), is(OK.value()));
        assertThat(coRespondentSubmissionResponse.path(ID), is(caseDetails.getId()));

        checkCaseAfterSuccessfulCoRespondentSubmission(userDetails, String.valueOf(caseDetails.getId()), coRespondentAnswersJson);
    }

    @Test
    public void givenCaseIsAosAwaiting_whenSubmittingCoRespondentAnswers_thenStateShouldNotChange() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));
        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_AWAITING_EVENT_ID, userDetails);

        final String coRespondentAnswersJson = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_ANSWERS_JSON_FILE_PATH);
        submitCoRespondentAosCase(userDetails, coRespondentAnswersJson);

        final Response caseRetrieval = retrieveAosCase(userDetails.getAuthToken());
        assertThat(caseRetrieval.getStatusCode(), is(OK.value()));
        assertThat(caseRetrieval.path(CASE_ID_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat("The state should never change on co-respondent submission", caseRetrieval.path(STATE_KEY), is(AOS_AWAITING));
    }

    @Test
    public void givenCaseIsAosStarted_whenSubmittingCoRespondentAnswers_thenStateShouldNotChange() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));
        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        final String coRespondentAnswersJson = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_ANSWERS_JSON_FILE_PATH);
        submitCoRespondentAosCase(userDetails, coRespondentAnswersJson);

        final Response caseRetrieval = retrieveAosCase(userDetails.getAuthToken());
        assertThat(caseRetrieval.getStatusCode(), is(OK.value()));
        assertThat(caseRetrieval.path(CASE_ID_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat("The state should never change on co-respondent submission", caseRetrieval.path(STATE_KEY), is(AOS_STARTED));
    }

    @Test
    public void givenCaseIsAosSubmittedAwaitingAnswer_whenSubmittingCoRespondentAnswers_thenStateShouldNotChange() throws Exception {
        final UserDetails userDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));
        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        final String respondentJson = loadJson(RESPONDENT_PAYLOAD_CONTEXT_PATH + AOS_DEFEND_CONSENT_JSON_FILE_PATH);
        submitRespondentAosCase(userDetails.getAuthToken(), caseDetails.getId(), respondentJson);

        final String coRespondentAnswersJson = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_ANSWERS_JSON_FILE_PATH);
        submitCoRespondentAosCase(userDetails, coRespondentAnswersJson);

        final Response caseRetrieval = retrieveAosCase(userDetails.getAuthToken());
        assertThat(caseRetrieval.getStatusCode(), is(OK.value()));
        assertThat(caseRetrieval.path(CASE_ID_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat("The state should never change on co-respondent submission", caseRetrieval.path(STATE_KEY), is(AOS_SUBMITTED_AWAITING_ANSWER));
    }

    @Test
    public void givenCaseIsAosOverdue_whenSubmittingCoRespondentAnswers_thenStateShouldNotChange() throws Exception {
        final UserDetails userDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(RESPONDENT_EMAIL_ADDRESS, userDetails.getEmailAddress()));
        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_AWAITING_EVENT_ID, userDetails);
        updateCase(String.valueOf(caseDetails.getId()), null, NOT_RECEIVED_AOS_EVENT_ID);


        final String coRespondentAnswersJson = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_ANSWERS_JSON_FILE_PATH);
        submitCoRespondentAosCase(userDetails, coRespondentAnswersJson);

        final Response caseRetrieval = retrieveAosCase(userDetails.getAuthToken());
        assertThat(caseRetrieval.getStatusCode(), is(OK.value()));
        assertThat(caseRetrieval.path(CASE_ID_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat("The state should never change on co-respondent submission", caseRetrieval.path(STATE_KEY), is(AOS_OVERDUE));
    }

    @Test
    public void givenCaseIsAosDefended_whenSubmittingCoRespondentAnswers_thenStateShouldNotChange() throws Exception {
        final UserDetails userDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));


        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        final String respondentJson = loadJson(RESPONDENT_PAYLOAD_CONTEXT_PATH + AOS_DEFEND_CONSENT_JSON_FILE_PATH);
        submitRespondentAosCase(userDetails.getAuthToken(), caseDetails.getId(), respondentJson);

        updateCase(String.valueOf(caseDetails.getId()), null, "answerReceived");

        final String coRespondentAnswersJson = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_ANSWERS_JSON_FILE_PATH);
        submitCoRespondentAosCase(userDetails, coRespondentAnswersJson);

        final Response caseRetrieval = retrieveAosCase(userDetails.getAuthToken());
        assertThat(caseRetrieval.getStatusCode(), is(OK.value()));
        assertThat(caseRetrieval.path(CASE_ID_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat("The state should never change on co-respondent submission", caseRetrieval.path(STATE_KEY), is(DEFENDED));
    }

    /**
     * It is quite possible that this test could fail if run during the cut over between one day and the next.
     * I.e the clock on the running COS remote instance could have ticked over to 25th December but the test could still be running on 24th December
     * at 23:59.
     */
    @Test
    public void givenCoRespondentIsDefending_whenSubmittingCoRespondentAnswers_thenDueDateIsRecalculated() throws Exception {
        final UserDetails userDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));

        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        final String coRespondentAnswersJson = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_DEFENDED_ANSWERS_JSON_FILE_PATH);
        submitCoRespondentAosCase(userDetails, coRespondentAnswersJson);

        final Response cosResponse = retrieveAosCase(userDetails.getAuthToken());
        String responseJson = cosResponse.getBody().asString();
        String dueDateSet = objectMapper.readTree(responseJson)
            .get("data")
            .get("coRespondentAnswers")
            .get("aos")
            .get("dueDate")
            .asText();

        assertThat(dueDateSet, is(LocalDate.now().plusDays(21).format(CCD_DATE_FORMATTER)));
    }

    private void checkCaseAfterSuccessfulCoRespondentSubmission(final UserDetails userDetails, final String caseId, final String expectedAnswers)
        throws Exception {

        final Response cosResponse = retrieveAosCase(userDetails.getAuthToken());
        assertThat(cosResponse.getStatusCode(), is(OK.value()));
        assertThat(cosResponse.path(CASE_ID_KEY), is(caseId));

        String expectedCoRespondentAnswersJson = objectMapper
            .readTree(expectedAnswers.replaceAll(CO_RESPONDENT_DEFAULT_EMAIL, userDetails.getEmailAddress()))
            .get("coRespondentAnswers")
            .toString();

        String responseJson = cosResponse.getBody().asString();
        String actualCoRespondentAnswersJson = objectMapper.readTree(responseJson)
            .get("data")
            .get("coRespondentAnswers")
            .toString();

        JSONAssert.assertEquals(expectedCoRespondentAnswersJson, actualCoRespondentAnswersJson, false);
    }

}
