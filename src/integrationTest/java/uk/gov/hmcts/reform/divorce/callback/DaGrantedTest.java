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

public class DaGrantedTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/da-granted/da-granted.json";
    private static final String ERROR_CASE_RESPONSE = "fixtures/submit-dn/dn-submitted-error.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseData_whenDaGranted_thenReturnDaData() {
        Map<String, Object> caseData = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.daGranted(createCaseWorkerUser().getAuthToken(), caseData);
        assertNotNull(response.get(DATA));
        assertEquals((((Map<String, Object>)caseData.get(CASE_DETAILS)).get(CASE_DATA)), response.get(DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseWithoutEmail_whenDaGranted_thenReturnNotificationError() {

        Map<String, Object> caseWithoutEmailAddress = ResourceLoader
                .loadJsonToObject(ERROR_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient
                .daGranted(createCaseWorkerUser().getAuthToken(), caseWithoutEmailAddress);

        assertNull(response.get(DATA));
        List<String> error = (List<String>) response.get(ERRORS);
        assertEquals(1,error.size());
        assertTrue(error.get(0).contains("email_address is a required property"));
    }
}
