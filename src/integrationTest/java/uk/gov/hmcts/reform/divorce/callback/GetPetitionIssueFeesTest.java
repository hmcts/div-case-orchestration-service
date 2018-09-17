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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;

public class GetPetitionIssueFeesTest extends IntegrationTest {

    private static final String DATA_KEY = "data";
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/solicitor/";

    @Value("${case.orchestration.solicitor.petition-issue-fees.context-path}")
    private String contextPath;

    @Test
    public void givenCreateEvent_whenGetPetitionIssueFees_thenReturnUpdatedData() throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        Response response = RestUtil.postToRestService(
                serverUrl + contextPath,
                headers,
                ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + "ccd-request-data.json")
        );

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Map<String, Object> responseData = response.getBody().path(DATA_KEY);

        assertNotNull(responseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY));
    }

}
