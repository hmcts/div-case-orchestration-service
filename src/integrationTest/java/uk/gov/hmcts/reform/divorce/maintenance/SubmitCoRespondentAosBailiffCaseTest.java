package uk.gov.hmcts.reform.divorce.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.category.ExtendedTest;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.RetrieveAosCaseSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.ISSUED_TO_BAILIFF;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.events.CcdTestEvents.TEST_AWAITING_DECREE_ABSOLUTE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.events.CcdTestEvents.TEST_ISSUED_TO_BAILIFF_EVENT;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

@Slf4j
public class SubmitCoRespondentAosBailiffCaseTest extends RetrieveAosCaseSupport {

    private static final String CO_RESPONDENT_PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/co-respondent/";
    private static final String SUBMIT_COMPLETE_CASE_JSON_FILE_PATH = "submit-complete-case.json";
    private static final String CO_RESP_ANSWERS_JSON_FILE_PATH = "co-respondent-answers.json";
    private static final String CO_RESPONDENT_ANSWERS_JSON;
    private UserDetails userDetails;
    private CaseDetails caseDetails;

    static {
        try {
            CO_RESPONDENT_ANSWERS_JSON = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_ANSWERS_JSON_FILE_PATH);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        userDetails = createCitizenUser();
    }

    @Test
    @Category(ExtendedTest.class)
    public void givenCaseIsDisallowedState_whenSubmittingCoRespondentAnswers_thenReturnBadRequest() throws Exception {
        caseDetails = getCaseDetailsAfterSubmitCase();
        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AWAITING_DECREE_ABSOLUTE_EVENT, userDetails);

        final String coRespondentAnswersJson = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + CO_RESP_ANSWERS_JSON_FILE_PATH);
        Response coRespondentSubmissionResponse = submitCoRespondentAosCase(userDetails, coRespondentAnswersJson);

        assertThat(coRespondentSubmissionResponse.getStatusCode(), is(BAD_REQUEST.value()));
    }

    @Test
    public void canSubmitAndRetrieveCoRespondentAnswers() throws Exception {
        caseDetails = getCaseDetailsAfterSubmitCase();
        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_ISSUED_TO_BAILIFF_EVENT, userDetails);

        final Response coRespondentSubmissionResponse = submitCoRespondentAosCase(userDetails, CO_RESPONDENT_ANSWERS_JSON);

        assertThat(coRespondentSubmissionResponse.getStatusCode(), is(OK.value()));
        assertThat(coRespondentSubmissionResponse.path(ID), is(caseDetails.getId()));

        checkCaseAfterSuccessfulCoRespondentSubmission(userDetails, String.valueOf(caseDetails.getId()), CO_RESPONDENT_ANSWERS_JSON);
    }

    @Test
    @Category(ExtendedTest.class)
    public void givenCaseIsIssuedToBailiff_whenSubmittingCoRespondentAnswers_thenStateShouldNotChange() {
        caseDetails = getCaseDetailsAfterSubmitCase();
        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_ISSUED_TO_BAILIFF_EVENT, userDetails);

        submitCoRespondentAosCase(userDetails, CO_RESPONDENT_ANSWERS_JSON);

        final Response caseRetrieval = retrieveAosCase(userDetails.getAuthToken());
        assertThat(caseRetrieval.getStatusCode(), is(OK.value()));
        assertThat(caseRetrieval.path(CASE_ID_JSON_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat("The state should never change on co-respondent submission", caseRetrieval.path(CASE_STATE_JSON_KEY), is(ISSUED_TO_BAILIFF));
    }

    private CaseDetails getCaseDetailsAfterSubmitCase() {
        CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, userDetails,
            Pair.of(CO_RESP_EMAIL_ADDRESS, userDetails.getEmailAddress()));
        log.info("Case " + caseDetails.getId() + " created.");
        return caseDetails;
    }

    private void checkCaseAfterSuccessfulCoRespondentSubmission(final UserDetails userDetails, final String caseId, final String expectedAnswers)
        throws Exception {

        Response cosResponse = retrieveAosCase(userDetails.getAuthToken());
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
