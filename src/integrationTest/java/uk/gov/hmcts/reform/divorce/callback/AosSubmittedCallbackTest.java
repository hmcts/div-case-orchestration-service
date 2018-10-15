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

public class AosSubmittedCallbackTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/respond-to-a-divorce/ccd-callback-aos-submitted.json";
    private static final String ERROR_CASE_RESPONSE = "fixtures/respond-to-a-divorce/ccd-callback-aos-submitted-error.json";
    private static final String CASE_DATA = "case_data";
    private static final String ERRORS = "errors";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void whenSubmitAOSSubmittedIsCalledBack_thenReturnAOSData() {
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.aosSubmitted(null, aosCase);
        assertEquals(aosCase.get(CASE_DATA), response.get(CASE_DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseWithoutEmail_whenAOSSubmittedIsCalledBack_thenReturnNotificationError() {
        Map<String, Object> aosCaseWithoutEmailAddress = ResourceLoader
                .loadJsonToObject(ERROR_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.aosSubmitted(null, aosCaseWithoutEmailAddress);

        assertNull(response.get(CASE_DATA));
        List<String> error = (List<String>) response.get(ERRORS);
        assertEquals(1, error.size());
        assertTrue(error.get(0).contains("Failed to send e-mail"));
    }

}