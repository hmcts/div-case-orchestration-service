package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class DaAboutToBeGrantedTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/da-about-to-be-granted/da-about-to-be-granted.json";
    private static final String DA_GRANTED_DATE = "DecreeAbsoluteGrantedDate";
    private static final String DOCUMENTS_GENERATED = "D8DocumentsGenerated";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenValidCaseData_whenDaAboutToBeGranted_thenReturnDaAboutToBeGrantedData() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, CcdCallbackRequest.class);
        Map<String, Object> response = cosApiClient.daAboutToBeGranted(
                createCaseWorkerUser().getAuthToken(),
                ccdCallbackRequest);
        assertNotNull(((Map)response.get(DATA)).get(DA_GRANTED_DATE));
        assertNotNull(((Map)response.get(DATA)).get(DOCUMENTS_GENERATED));
    }
}
