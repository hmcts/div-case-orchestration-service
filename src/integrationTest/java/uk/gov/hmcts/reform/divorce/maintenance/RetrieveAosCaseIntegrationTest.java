package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.callback.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJsonToObject;

public class RetrieveAosCaseIntegrationTest extends CcdSubmissionSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/retrieve-aos-case/";
    private static final String CASE_ID_KEY = "caseId";
    private static final String TEST_AOS_RESPONDED_EVENT = "testAosResponded";
    private static final String COURTS_KEY = "courts";
    private static final String STATE_KEY = "state";
    private static final String DATA_KEY = "data";

    @Value("${case.orchestration.maintenance.retrieve-aos-case.context-path}")
    private String contextPath;

    @Test
    public void givenUserTokenIsNull_whenAuthenticateUser_thenReturnBadRequest() {
        Response cosResponse = retrieveAosCase(null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenNoCase_whenRetrieveAosCase_thenReturnEmptyResponse() {
        Response cosResponse = retrieveAosCase(createCitizenUser().getAuthToken());

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertNull(cosResponse.path(CASE_ID_KEY));
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
        assertEquals("AosResponded", cosResponse.path(STATE_KEY));
        assertNotNull(cosResponse.path(DATA_KEY));
    }

    private Response retrieveAosCase(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.getFromRestService(
                serverUrl + contextPath,
                headers,
                Collections.singletonMap("checkCcd", true)
        );
    }
}
