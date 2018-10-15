package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PetitionSubmissionNotificationTest extends IntegrationTest {

    private static final String DATA_KEY = "data";
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/issue-petition/";

    @Value("${case.orchestration.petition-submission.notification.context-path}")
    private String contextPath;

    @Test
    public void givenCreateEvent_whenPetitionSubmitted_thenReturnCallbackResponse() throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        Response response = RestUtil.postToRestService(
                serverUrl + contextPath,
                headers,
                ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + "ccd-callback-petition-issued.json")
        );

        // Will fail if email fails to send
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Map<String, Object> responseData = response.getBody().path(DATA_KEY);

        assertNotNull(responseData);
    }

}