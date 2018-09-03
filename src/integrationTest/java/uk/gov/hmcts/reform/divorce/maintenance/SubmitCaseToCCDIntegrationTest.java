package uk.gov.hmcts.reform.divorce.maintenance;

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
import static org.junit.Assert.assertNotEquals;

public class SubmitCaseToCCDIntegrationTest extends IntegrationTest {

    private static final String CASE_ID_KEY = "caseId";
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit/";

    @Value("${case.orchestration.maintenance.submit.context-path}")
    private String contextPath;

    @Test
    public void givenDivorceSession_whenSubmitIsCalled_caseIdIsReturned() throws Exception {
        Response submissionResponse = submitCase(createCitizenUser().getAuthToken(), "basic-divorce-session.json");

        assertEquals(HttpStatus.OK.value(), submissionResponse.getStatusCode());
        assertNotEquals("0", submissionResponse.path(CASE_ID_KEY));
    }

    private Response submitCase(String userToken, String fileName) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.postToRestService(
                serverUrl + contextPath,
                headers,
                fileName == null ? null : ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + fileName)
        );
    }
}
