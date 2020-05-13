package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

        CcdCallbackResponse response = cosApiClient.handleDaGranted(
            createCaseWorkerUser().getAuthToken(),
            ccdCallbackRequest);

        Map<String, Object> responseData = response.getData();

        assertNotNull(responseData);
        assertEquals(response.getErrors().size(), 0);
        assertNoExtraDocumentsWhereGenerated((List) responseData.get(D8DOCUMENTS_GENERATED));
    }

    private static void assertNoExtraDocumentsWhereGenerated(List<CollectionMember<Document>> documents) {
        assertEquals(1, documents.size());
    }
}
