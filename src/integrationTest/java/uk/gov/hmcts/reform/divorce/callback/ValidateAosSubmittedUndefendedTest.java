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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class ValidateAosSubmittedUndefendedTest extends IntegrationTest {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/validate-event/";

    private static final String CASE_ERROR_KEY = "errors";
    private static final List<String> EXPECTED_ERROR =
        Collections.singletonList("aosReceivedNoAdConStarted event should be used for AOS submission in this case");

    @Value("${case.orchestration.validate-aos-submitted-undefended.context-path}")
    private String contextPath;

    @Test
    public void givenAosSubmittedUndefendedEventAdulteryCase_WhenValidated_thenReturnError() throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        Response response = RestUtil.postToRestService(
                serverUrl + contextPath,
                headers,
                ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + "aos-submitted-undefended-adultery.json")
        );

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(EXPECTED_ERROR, response.path(CASE_ERROR_KEY));
    }

}
