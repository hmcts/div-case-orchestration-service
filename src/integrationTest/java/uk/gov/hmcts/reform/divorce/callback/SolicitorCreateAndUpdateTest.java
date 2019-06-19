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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;

public class SolicitorCreateAndUpdateTest extends IntegrationTest {

    private static final String DATA_KEY = "data";
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/solicitor/";

    @Value("${case.orchestration.solicitor.solicitor-create.context-path}")
    private String solicitorCreatePath;

    @Value("${case.orchestration.solicitor.solicitor-update.context-path}")
    private String solicitorUpdatePath;

    @Test
    public void givenCallbackRequest_whenSolicitorCreate_thenReturnUpdatedData() throws Exception {
        Response response = postWithDataAndValidateResponse(
                serverUrl + solicitorCreatePath,
                PAYLOAD_CONTEXT_PATH + "solicitor-request-data.json",
                createSolicitorUser().getAuthToken()
        );

        assertEverythingIsFine(response);
    }

    @Test
    public void givenCallbackRequest_whenSolicitorUpdate_thenReturnUpdatedData() throws Exception {
        Response response = postWithDataAndValidateResponse(
                serverUrl + solicitorUpdatePath,
                PAYLOAD_CONTEXT_PATH + "solicitor-request-data.json",
                createSolicitorUser().getAuthToken()
        );

        assertEverythingIsFine(response);
    }

    private static void assertEverythingIsFine(Response response) {
        Map<String, Object> responseData = response.getBody().path(DATA_KEY);

        assertNotNull(responseData.get(CREATED_DATE_JSON_KEY));
        assertNotNull(responseData.get(DIVORCE_UNIT_JSON_KEY));
        assertNotNull(responseData.get(DIVORCE_CENTRE_SITEID_JSON_KEY));
    }

    static Response postWithDataAndValidateResponse(
            String url, String pathToFileWithData, String authToken) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, authToken);

        Response response = RestUtil.postToRestService(url, headers, ResourceLoader.loadJson(pathToFileWithData));

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        return response;
    }
}
