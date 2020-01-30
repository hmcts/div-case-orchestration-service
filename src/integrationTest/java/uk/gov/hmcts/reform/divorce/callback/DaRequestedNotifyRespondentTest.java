package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class DaRequestedNotifyRespondentTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/da-requested/da-requested.json";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenValidCaseData_whenNotifyRespondentOfDARequested_thenReturnDaRequestedByApplicantData() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, CcdCallbackRequest.class);
        Map<String, Object> response = cosApiClient.notifyRespondentOfDARequested(
                createCaseWorkerUser().getAuthToken(),
                ccdCallbackRequest);
        assertNotNull(response.get(DATA));
    }
}
