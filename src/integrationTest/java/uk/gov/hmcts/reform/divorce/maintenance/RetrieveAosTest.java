package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.RetrieveAosCaseSupport;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJsonToObject;

public class RetrieveAosTest extends RetrieveAosCaseSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/retrieve-aos-case/";
    private static final String TEST_AOS_RESPONDED_EVENT = "testAosStarted";
    private static final String COURTS_KEY = "courts";
    private static final String STATE_KEY = "state";
    private static final String DATA_KEY = "data";

    @Test
    public void givenUserTokenIsNull_whenAuthenticateUser_thenReturnBadRequest() {
        Response cosResponse = retrieveAosCase(null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenNoCase_whenRetrieveAosCase_thenReturnEmptyResponse() {
        Response cosResponse = retrieveAosCase(createCitizenUser().getAuthToken());

        assertEquals(HttpStatus.NOT_FOUND.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenCaseExists_whenRetrieveAosCase_thenReturnResponse() {
        UserDetails userDetails = createCitizenUser();

        CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        caseDetails = updateCaseForCitizen(String.valueOf(caseDetails.getId()), null,
            TEST_AOS_RESPONDED_EVENT, userDetails);

        Response cosResponse = retrieveAosCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals(String.valueOf(caseDetails.getId()), cosResponse.path(CASE_ID_KEY));
        assertEquals("eastMidlands", cosResponse.path(COURTS_KEY));
        assertEquals("AosStarted", cosResponse.path(STATE_KEY));
        assertEquals(loadJsonToObject(PAYLOAD_CONTEXT_PATH + "aos-divorce-session.json", Map.class),
            cosResponse.path(DATA_KEY));
    }
}
