package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJsonToObject;

public class RetrieveCaseTest extends RetrieveCaseSupport {
    private static final String CASE_ID_KEY = "caseId";

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/retrieve-case/";
    private static final String COURTS_KEY = "courts";
    private static final String STATE_KEY = "state";
    private static final String DATA_KEY = "data";

    @Test
    public void givenUserTokenIsNull_whenRetrieveCase_thenReturnBadRequest() {
        Response cosResponse = retrieveCase(null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenNoCase_whenRetrieveCase_thenReturnEmptyResponse() {
        Response cosResponse = retrieveCase(createCitizenUser().getAuthToken());

        assertEquals(HttpStatus.NOT_FOUND.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenCaseExists_whenRetrieveCase_thenReturnResponse() {
        UserDetails userDetails = createCitizenUser();

        CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        Response cosResponse = retrieveCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals(String.valueOf(caseDetails.getId()), cosResponse.path(CASE_ID_KEY));
        assertEquals("eastMidlands", cosResponse.path(COURTS_KEY));
        assertEquals("AwaitingPayment", cosResponse.path(STATE_KEY));
        assertEquals(loadJsonToObject(PAYLOAD_CONTEXT_PATH + "divorce-session.json", Map.class),
            cosResponse.path(DATA_KEY));
    }


    @Test
    public void givenMultipleSubmittedCaseInCcd_whenGetCase_thenReturn300() {
        UserDetails userDetails = createCitizenUser();

        submitCase("submit-complete-case.json", userDetails);
        submitCase("submit-complete-case.json", userDetails);

        Response cosResponse = retrieveCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(), cosResponse.getStatusCode());
        assertEquals(cosResponse.getBody().asString(), "");
    }

}