package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.junit.Assert.*;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;

public class DaGrantedTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/da-granted/da-granted-digital-no.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenValidCaseData_whenDaIsGranted_thenReturnDaGrantedData() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, CcdCallbackRequest.class);

        ResponseEntity<CcdCallbackResponse> response = cosApiClient.handleDaGranted(
            createCaseWorkerUser().getAuthToken(),
            ccdCallbackRequest);

        Map<String, Object> responseData = response.getBody().getData();
        Map<String, Object> requestData = ccdCallbackRequest.getCaseDetails().getCaseData();

        assertTrue("Status code should be 200", response.getStatusCode().is2xxSuccessful());
        assertNotNull("Case data in response should not be null", responseData);
        assertEquals("Response data should be the same as the payload sent", requestData, responseData);
        assertEquals("No errors should be returned", response.getBody().getErrors().size(), 0);

        assertNoDocumentsGeneratedByWorkflow_WasSavedInCasedata(responseData);
    }

    private void assertNoDocumentsGeneratedByWorkflow_WasSavedInCasedata(Map<String, Object> responseData) {
        List<CollectionMember<Document>> documents = (List<CollectionMember<Document>>) responseData.get(D8DOCUMENTS_GENERATED);
        assertEquals("No addition document entry should be made to case data", 1, documents.size());
    }
}
