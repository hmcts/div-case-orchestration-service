package uk.gov.hmcts.reform.divorce.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.category.ExtendedTest;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.RetrieveAosCaseSupport;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.DEFENDED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
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
    private static final String CO_RESPONDENT_ANSWERS_JSON;

    static {
        try {
            CO_RESPONDENT_ANSWERS_JSON = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_ANSWERS_JSON_FILE_PATH);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Category(ExtendedTest.class)
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

        final Response coRespondentSubmissionResponse = submitCoRespondentAosCase(userDetails, CO_RESPONDENT_ANSWERS_JSON);

        assertThat(coRespondentSubmissionResponse.getStatusCode(), is(OK.value()));
        assertThat(coRespondentSubmissionResponse.path(ID), is(caseDetails.getId()));

        checkCaseAfterSuccessfulCoRespondentSubmission(userDetails, String.valueOf(caseDetails.getId()), CO_RESPONDENT_ANSWERS_JSON);
    }

    @Test
    @Category(ExtendedTest.class)
    public void givenCaseIsAosAwaiting_whenSubmittingCoRespondentAnswers_thenStateShouldNotChange() {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));
        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_AWAITING_EVENT_ID, userDetails);

        submitCoRespondentAosCase(userDetails, CO_RESPONDENT_ANSWERS_JSON);

        final Response caseRetrieval = retrieveAosCase(userDetails.getAuthToken());
        assertThat(caseRetrieval.getStatusCode(), is(OK.value()));
        assertThat(caseRetrieval.path(CASE_ID_JSON_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat("The state should never change on co-respondent submission", caseRetrieval.path(CASE_STATE_JSON_KEY), is(AOS_AWAITING));
    }

    @Test
    @Category(ExtendedTest.class)
    public void givenCaseIsAosStarted_whenSubmittingCoRespondentAnswers_thenStateShouldNotChange() {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));
        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        submitCoRespondentAosCase(userDetails, CO_RESPONDENT_ANSWERS_JSON);

        final Response caseRetrieval = retrieveAosCase(userDetails.getAuthToken());
        assertThat(caseRetrieval.getStatusCode(), is(OK.value()));
        assertThat(caseRetrieval.path(CASE_ID_JSON_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat("The state should never change on co-respondent submission", caseRetrieval.path(CASE_STATE_JSON_KEY), is(AOS_STARTED));
    }

    @Test
    @Category(ExtendedTest.class)
    public void givenCaseIsAosSubmittedAwaitingAnswer_whenSubmittingCoRespondentAnswers_thenStateShouldNotChange() throws Exception {
        final UserDetails userDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));
        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        final String respondentJson = loadJson(RESPONDENT_PAYLOAD_CONTEXT_PATH + AOS_DEFEND_CONSENT_JSON_FILE_PATH);
        submitRespondentAosCase(userDetails.getAuthToken(), caseDetails.getId(), respondentJson);

        submitCoRespondentAosCase(userDetails, CO_RESPONDENT_ANSWERS_JSON);

        final Response caseRetrieval = retrieveAosCase(userDetails.getAuthToken());
        assertThat(caseRetrieval.getStatusCode(), is(OK.value()));
        assertThat(caseRetrieval.path(CASE_ID_JSON_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat("The state should never change on co-respondent submission",
            caseRetrieval.path(CASE_STATE_JSON_KEY), is(AOS_SUBMITTED_AWAITING_ANSWER));
    }

    @Test
    @Category(ExtendedTest.class)
    public void givenCaseIsAosOverdue_whenSubmittingCoRespondentAnswers_thenStateShouldNotChange() {
        final UserDetails userDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(RESPONDENT_EMAIL_ADDRESS, userDetails.getEmailAddress()));
        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_AWAITING_EVENT_ID, userDetails);
        updateCase(String.valueOf(caseDetails.getId()), null, NOT_RECEIVED_AOS_EVENT_ID);


        submitCoRespondentAosCase(userDetails, CO_RESPONDENT_ANSWERS_JSON);

        final Response caseRetrieval = retrieveAosCase(userDetails.getAuthToken());
        assertThat(caseRetrieval.getStatusCode(), is(OK.value()));
        assertThat(caseRetrieval.path(CASE_ID_JSON_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat("The state should never change on co-respondent submission", caseRetrieval.path(CASE_STATE_JSON_KEY), is(AOS_OVERDUE));
    }

    @Test
    @Category(ExtendedTest.class)
    public void givenCaseIsAosDefended_whenSubmittingCoRespondentAnswers_thenStateShouldNotChange() throws Exception {
        final UserDetails userDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));

        log.info("Case " + caseDetails.getId() + " created.");

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        final String respondentJson = loadJson(RESPONDENT_PAYLOAD_CONTEXT_PATH + AOS_DEFEND_CONSENT_JSON_FILE_PATH);
        submitRespondentAosCase(userDetails.getAuthToken(), caseDetails.getId(), respondentJson);

        updateCase(String.valueOf(caseDetails.getId()), null, "answerReceived");

        submitCoRespondentAosCase(userDetails, CO_RESPONDENT_ANSWERS_JSON);

        final Response caseRetrieval = retrieveAosCase(userDetails.getAuthToken());
        assertThat(caseRetrieval.getStatusCode(), is(OK.value()));
        assertThat(caseRetrieval.path(CASE_ID_JSON_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat("The state should never change on co-respondent submission", caseRetrieval.path(CASE_STATE_JSON_KEY), is(DEFENDED));
    }

    /**
     * It is quite possible that this test could fail if run during the cut over between one day and the next.
     * I.e the clock on the running COS remote instance could have ticked over to 25th December but the test could still be running on 24th December
     * at 23:59.
     */
    @Test
    @Category(ExtendedTest.class)
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
        assertThat(cosResponse.path(CASE_ID_JSON_KEY), is(caseId));

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
