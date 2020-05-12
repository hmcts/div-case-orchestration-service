package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

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

        System.out.println(response); //TODO ...

    }
}
