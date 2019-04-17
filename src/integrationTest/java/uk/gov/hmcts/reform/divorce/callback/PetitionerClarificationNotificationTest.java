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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class PetitionerClarificationNotificationTest extends IntegrationTest {

    private static final String DATA_KEY = "data";
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/legal-advisor/";

    @Value("${case.orchestration.petitioner-clarification.notification.context-path}")
    private String contextPath;

    @Test
    public void happyPath() throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        Response response = RestUtil.postToRestService(
                serverUrl + contextPath,
                headers,
                ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + "callback-petitioner-clarification-requested.json")
        );

        // Will fail if email fails to send
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        final Map<String, Object> responseData = response.getBody().path(DATA_KEY);

        assertThat(responseData, is(notNullValue()));
    }

}
