package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;

public class DaGrantedTest extends IntegrationTest {

    private static final String DA_GRANTED_CALLBACK_REQUEST = "fixtures/da-granted/da-granted-offline.json";
    private static final String DA_GRANTED_REPRESENTED_CALLBACK_REQUEST = "fixtures/da-granted/da-granted-offline-represented-respondent.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenValidCaseData_whenDaIsGranted_ForRespondent_thenReturnDaGrantedData() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(DA_GRANTED_CALLBACK_REQUEST, CcdCallbackRequest.class);

        ResponseEntity<CcdCallbackResponse> response = cosApiClient.handleDaGranted(
            createCaseWorkerUser().getAuthToken(),
            ccdCallbackRequest);

        Map<String, Object> responseData = response.getBody().getData();

        assertEquals("Status code should be 200", response.getStatusCode(), HttpStatus.OK);
        assertNotNull("Case data in response should not be null", responseData);
        assertNull("No errors should be returned", response.getBody().getErrors());

        assertNoDocumentsGeneratedByWorkflowWasSavedInCaseData(responseData);
    }

    @Test
    public void givenValidCaseData_whenDaIsGranted_ForRepresentedRespondent_thenReturnDaGrantedData() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(DA_GRANTED_REPRESENTED_CALLBACK_REQUEST, CcdCallbackRequest.class);

        ResponseEntity<CcdCallbackResponse> response = cosApiClient.handleDaGranted(
            createCaseWorkerUser().getAuthToken(),
            ccdCallbackRequest);

        Map<String, Object> responseData = response.getBody().getData();

        assertEquals("Status code should be 200", response.getStatusCode(), HttpStatus.OK);
        assertNotNull("Case data in response should not be null", responseData);
        assertNull("No errors should be returned", response.getBody().getErrors());

        assertNoDocumentsGeneratedByWorkflowWasSavedInCaseData(responseData);
    }

    private void assertNoDocumentsGeneratedByWorkflowWasSavedInCaseData(Map<String, Object> responseData) {
        List<CollectionMember<Document>> documents = (List<CollectionMember<Document>>) responseData.get(D8DOCUMENTS_GENERATED);
        assertEquals("No addition document entry should be made to case data", 1, documents.size());
    }

}