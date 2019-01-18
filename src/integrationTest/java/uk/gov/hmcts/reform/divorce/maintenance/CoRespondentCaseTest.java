package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.RetrieveAosCaseSupport;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.context.ServiceContextConfiguration.OBJECT_MAPPER;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

public class CoRespondentCaseTest extends RetrieveAosCaseSupport {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/co-respondent/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String STATE_KEY = "state";

    @Test
    public void givenNoConsentAndNoDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);
        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        String requestBodyJson = loadJson(PAYLOAD_CONTEXT_PATH + "co-respondent-answers.json");
        updateCaseWithCoRespondentAnswers(userDetails, caseDetails, requestBodyJson);

        Response cosResponse = retrieveAosCase(userDetails.getAuthToken());
        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals(String.valueOf(caseDetails.getId()), cosResponse.path(CASE_ID_KEY));
        assertEquals("DNAwaiting", cosResponse.path(STATE_KEY));

        String requestCoRespondentAnswersJson = OBJECT_MAPPER.readTree(requestBodyJson)
                .get("coRespondentAnswers")
                .toString();
        String responseJson = cosResponse.getBody().asString();
        String responseCoRespondentAnswersJson = OBJECT_MAPPER.readTree(responseJson)
                .get("data")
                .get("coRespondentAnswers")
                .toString();
        JSONAssert.assertEquals(requestCoRespondentAnswersJson, responseCoRespondentAnswersJson, false);
    }

    private void updateCaseWithCoRespondentAnswers(UserDetails userDetails, CaseDetails caseDetails, String requestBody) {
        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(), requestBody);

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        assertEquals("AwaitingDecreeNisi", cosResponse.path(STATE_KEY));
    }

}