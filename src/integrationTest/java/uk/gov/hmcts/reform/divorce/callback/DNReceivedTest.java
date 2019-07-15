package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_DN_ANSWERS;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class DNReceivedTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/submit-dn/dn-submitted.json";
    private static final String ERROR_CASE_RESPONSE = "fixtures/submit-dn/dn-submitted-error.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenCase_whenDNSubmitted_thenReturnDNDataPlusAnswerDoc() {
        Map<String, Object> dnCase = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.dnSubmitted(createCaseWorkerUser().getAuthToken(), dnCase);
        Map<String, Object> resData = (Map<String, Object>) response.get(DATA);
        String jsonResponse = objectToJson(response);
        assertNotNull(resData);
        assertThat(jsonResponse, hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentFileName",
            is(DOCUMENT_TYPE_DN_ANSWERS)));
        assertThat(jsonResponse, hasJsonPath("$.data.D8caseReference",
            is("LV17D80100")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseWithoutEmail_whenDNSubmitted_thenReturnDNDataPlusAnswerDoc() {
        Map<String, Object> dnCaseWithoutEmailAddress = ResourceLoader
                .loadJsonToObject(ERROR_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient
                .dnSubmitted(createCaseWorkerUser().getAuthToken(), dnCaseWithoutEmailAddress);
        Map<String, Object> resData = (Map<String, Object>) response.get(DATA);
        String jsonResponse = objectToJson(response);
        
        assertNotNull(resData);
        assertThat(jsonResponse, hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentFileName",
            is(DOCUMENT_TYPE_DN_ANSWERS)));
        assertThat(jsonResponse, hasJsonPath("$.data.D8caseReference",
            is("LV17D80100")));
    }
}
