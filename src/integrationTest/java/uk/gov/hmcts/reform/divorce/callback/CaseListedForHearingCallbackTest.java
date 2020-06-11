package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;

public class CaseListedForHearingCallbackTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/case-linked-for-hearing/case-linked-for-hearing.json";
    private static final String DIGITAL_NO_RESPONSE = "fixtures/case-linked-for-hearing/case-linked-for-hearing-digital-no.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void whenCaseLinkedForHearingIsCalledBack_thenReturnAOSData() {
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        ResponseEntity<CcdCallbackResponse> response = cosApiClient.caseLinkedForHearing(
            createCaseWorkerUser().getAuthToken(),
            aosCase);
        Map<String, Object> responseData = response.getBody().getData();

        assertNotNull(responseData);
        assertEquals(((Map<String, Object>)aosCase.get(CASE_DETAILS)).get(CASE_DATA), responseData);
    }

    @Test
    public void givenValidCaseData_whenCaseListedForHearing_thenReturnValidData() {
        Map<String, Object> caseData = ResourceLoader.loadJsonToObject(DIGITAL_NO_RESPONSE, Map.class);

        ResponseEntity<CcdCallbackResponse> response = cosApiClient.caseLinkedForHearing(
            createCaseWorkerUser().getAuthToken(),
            caseData);

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
