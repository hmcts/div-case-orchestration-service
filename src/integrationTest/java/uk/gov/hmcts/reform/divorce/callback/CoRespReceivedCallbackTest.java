package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CoRespReceivedCallbackTest extends IntegrationTest {

    private static final String CO_DEFENDED_CASE_RESPONSE = "fixtures/co-resp-case/co-resp-defend.json";
    private static final String CO_UNDEFENDED_CASE_RESPONSE = "fixtures/co-resp-case/co-resp-undefend.json";
    private static final String CO_UNDEFENDED_RESP_RESPONDED_CASE_RESPONSE = "fixtures/co-resp-case/co-resp-undefend-resp-responds.json";

    private static final String ERROR_CASE_RESPONSE = "fixtures/co-resp-case/co-resp-received-error.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenUndefendedCase_whenSubmitAOS_thenReturnAOSData() {
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(CO_UNDEFENDED_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.coRespReceived(createCaseWorkerUser().getAuthToken(), aosCase);
        assertNotNull(response.get(DATA));
        assertEquals(((Map<String, Object>)aosCase.get(CASE_DETAILS)).get(CASE_DATA), response.get(DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenUndefendedRespondedCase_whenSubmitAOS_thenReturnAOSData() {
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(CO_UNDEFENDED_RESP_RESPONDED_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.coRespReceived(createCaseWorkerUser().getAuthToken(), aosCase);
        assertNotNull(response.get(DATA));
        assertEquals(((Map<String, Object>)aosCase.get(CASE_DETAILS)).get(CASE_DATA), response.get(DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenDefendedCase_whenSubmitAOS_thenReturnAOSData() {
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(CO_DEFENDED_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.coRespReceived(createCaseWorkerUser().getAuthToken(), aosCase);
        assertNotNull(response.get(DATA));
        assertEquals(((Map<String, Object>)aosCase.get(CASE_DETAILS)).get(CASE_DATA), response.get(DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseWithoutCoRespEmail_whenSubmitAOS_thenReturnNotificationError() {

        Map<String, Object> aosCaseWithoutCoRespEmailAddress = ResourceLoader
                .loadJsonToObject(ERROR_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient
                .coRespReceived(createCaseWorkerUser().getAuthToken(), aosCaseWithoutCoRespEmailAddress);

        assertNull(response.get(DATA));
        List<String> error = (List<String>) response.get(ERRORS);

        assertEquals(1,error.size());
        assertTrue(error.get(0).contains("email_address is a required property"));
    }
}
