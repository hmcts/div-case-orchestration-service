package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CoRespReceivedCallbackTest extends IntegrationTest {

    private static final String CO_UNDEFENDED_CASE_RESPONSE = "fixtures/co-resp-case/co-resp-undefend.json";

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
}
