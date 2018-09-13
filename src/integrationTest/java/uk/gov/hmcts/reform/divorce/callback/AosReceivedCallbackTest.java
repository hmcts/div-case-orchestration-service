package uk.gov.hmcts.reform.divorce.callback;

import com.fasterxml.jackson.databind.JsonNode;
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

public class AosReceivedCallbackTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/retrieve-aos-case/aos-received.json";
    private static final String ERROR_CASE_RESPONSE = "fixtures/retrieve-aos-case/aos-received-error.json";
    public static final String CASE_DATA = "case_data";
    public static final String ERRORS = "errors";
    public static final String CASE_DETAILS = "case_details";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCase_whenSubmitAOS_thenReturnAOSData() {

        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        Map<String, Object> response =cosApiClient.aosReceived(createCaseWorkerUser().getAuthToken(),aosCase);
        assertEquals(aosCase.get(CASE_DATA), response.get(CASE_DATA));
    }

    @Test
    public void givenCaseWithoutEmail_whenSubmitAOS_thenReturnNotificationError() {

        Map<String, Object> aosCaseWithoutEmailAddress = ResourceLoader.loadJsonToObject(ERROR_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.aosReceived(createCaseWorkerUser().getAuthToken(), aosCaseWithoutEmailAddress);

        assertNull(response.get(CASE_DATA));
        List<String> error = (List<String>) response.get(ERRORS);
        assertEquals(1,error.size());
        assertTrue(error.get(0).contains("email_address Not a valid email address"));
    }
}
