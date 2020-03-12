package uk.gov.hmcts.reform.divorce.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.RetrieveAosCaseSupport;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

public class RetrieveAosTest extends RetrieveAosCaseSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/retrieve-aos-case/";
    private static final String TEST_AOS_RESPONDED_EVENT = "testAosStarted";
    private static final String COURTS_KEY = "courts";
    private static final String DATA_KEY = "data";

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenCaseExists_whenRetrieveAosCase_thenReturnResponse() throws Exception {
        UserDetails userDetails = createCitizenUser();

        CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails,
            Pair.of(RESPONDENT_EMAIL_ADDRESS, userDetails.getEmailAddress()));

        caseDetails = updateCaseForCitizen(String.valueOf(caseDetails.getId()), null,
            TEST_AOS_RESPONDED_EVENT, userDetails);

        Response cosResponse = retrieveAosCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals(String.valueOf(caseDetails.getId()), cosResponse.path(CASE_ID_JSON_KEY));
        assertEquals(TEST_COURT, cosResponse.path(COURTS_KEY));
        assertEquals(AOS_STARTED, cosResponse.path(STATE_CCD_FIELD));
        String responseJson = cosResponse.getBody().asString();
        String responseJsonData = objectMapper.readTree(responseJson)
                .get(DATA_KEY)
                .toString();

        String expectedResponse = loadJson(PAYLOAD_CONTEXT_PATH + "aos-divorce-session.json")
            .replaceAll(RESPONDENT_DEFAULT_EMAIL, userDetails.getEmailAddress());
        JSONAssert.assertEquals(expectedResponse, responseJsonData, false);
    }
}
