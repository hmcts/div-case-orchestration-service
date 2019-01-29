package uk.gov.hmcts.reform.divorce.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.RetrieveAosCaseSupport;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

@Slf4j
public class CoRespondentCaseTest extends RetrieveAosCaseSupport {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/co-respondent/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String STATE_KEY = "state";

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenNoConsentAndNoDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);
        log.info("Case " + caseDetails.getId() + " created.");
        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        String requestBodyJson = loadJson(PAYLOAD_CONTEXT_PATH + "co-respondent-answers.json");
        updateCaseWithCoRespondentAnswers(userDetails, caseDetails, requestBodyJson);

        Response cosResponse = retrieveAosCase(userDetails.getAuthToken());
        assertThat(cosResponse.getStatusCode(), is(OK.value()));
        assertThat(cosResponse.path(CASE_ID_KEY), is(String.valueOf(caseDetails.getId())));
        assertThat(cosResponse.path(STATE_KEY), is("DNAwaiting"));

        String requestCoRespondentAnswersJson = objectMapper.readTree(requestBodyJson)
                .get("coRespondentAnswers")
                .toString();
        String responseJson = cosResponse.getBody().asString();
        String responseCoRespondentAnswersJson = objectMapper.readTree(responseJson)
                .get("data")
                .get("coRespondentAnswers")
                .toString();
        JSONAssert.assertEquals(requestCoRespondentAnswersJson, responseCoRespondentAnswersJson, false);
    }

    private void updateCaseWithCoRespondentAnswers(UserDetails userDetails, CaseDetails caseDetails, String requestBody) {
        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(), requestBody);

        assertThat(cosResponse.getStatusCode(), is(OK.value()));
        assertThat(cosResponse.path("id"), is(caseDetails.getId()));
        assertThat(cosResponse.path(STATE_KEY), is("AwaitingDecreeNisi"));
    }

}