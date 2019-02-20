package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CoRespReceivedCallbackTest extends IntegrationTest {

    private static final String CO_DEFENDED_CASE_RESPONSE = "fixtures/co-resp-case/co-resp-defend.json";
    private static final String CO_UNDEFENDED_CASE_RESPONSE = "fixtures/co-resp-case/co-resp-undefend.json";

    private static final String ERROR_CASE_RESPONSE = "fixtures/co-resp-case/co-resp-received-error.json";
    private static final String CASE_DATA = "case_data";
    private static final String ERRORS = "errors";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenUndefendedCase_whenSubmitAOS_thenReturnAOSData() {
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(CO_UNDEFENDED_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.coRespReceived(createCaseWorkerUser().getAuthToken(), aosCase);
        assertEquals(aosCase.get(CASE_DATA), response.get(CASE_DATA));
    }

    @Test
    public void givenDefendedCase_whenSubmitAOS_thenReturnAOSData() {
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(CO_DEFENDED_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.coRespReceived(createCaseWorkerUser().getAuthToken(), aosCase);
        assertEquals(aosCase.get(CASE_DATA), response.get(CASE_DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseWithoutEmail_whenSubmitAOS_thenReturnNotificationError() {

        Map<String, Object> aosCaseWithoutEmailAddress = ResourceLoader
                .loadJsonToObject(ERROR_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient
                .aosReceived(createCaseWorkerUser().getAuthToken(), aosCaseWithoutEmailAddress);

        assertNull(response.get(CASE_DATA));
        List<String> error = (List<String>) response.get(ERRORS);
        assertEquals(1,error.size());
        assertTrue(error.get(0).contains("email_address Not a valid email address"));
    }
}
