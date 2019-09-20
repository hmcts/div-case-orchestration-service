package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AosSubmittedCallbackTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/respond-to-a-divorce/ccd-callback-aos-submitted.json";
    private static final String ERROR_CASE_RESPONSE =
            "fixtures/respond-to-a-divorce/ccd-callback-aos-submitted-error.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void whenSubmitAOSSubmittedIsCalledBack_thenReturnAOSData() {
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.aosSubmitted(aosCase);
        assertNotNull(response.get(DATA));
        assertEquals(((Map<String, Object>)aosCase.get(CASE_DETAILS)).get(CASE_DATA), response.get(DATA));
    }
}