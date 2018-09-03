package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.ccd.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class UpdateCaseInCCDIntegrationTest extends IntegrationTest {

    private static final String UPDATE_EVENT_ID = "paymentMade";
    private static final String CASE_ID_KEY = "caseId";
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/update/";

    @Autowired
    private CcdClientSupport ccdClientSupport;

    @Value("${case.orchestration.maintenance.update.context-path}")
    private String contextPath;

    @Test
    public void givenDivorceSession_whenUpdateIsCalled_caseIdIsReturned() throws Exception {
        UserDetails citizenUser = createCitizenUser();
        String caseId = ccdClientSupport.submitCase(
                ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "submit-case-data.json", Map.class),
                citizenUser
        ).getId().toString();

        Response updateResponse = updateCase(citizenUser.getAuthToken(), caseId, "payments-update.json");

        assertEquals(HttpStatus.OK.value(), updateResponse.getStatusCode());
        assertNotEquals("0", updateResponse.path(CASE_ID_KEY));
    }

    private Response updateCase(String userToken, String caseId, String fileName) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.postToRestService(
                serverUrl + contextPath + "/" + caseId + "/" + UPDATE_EVENT_ID,
                headers,
                fileName == null ? null : ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + fileName)
        );
    }
}
