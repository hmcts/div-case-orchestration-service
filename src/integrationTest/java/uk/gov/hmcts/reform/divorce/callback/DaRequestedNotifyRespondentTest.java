package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DaRequestedNotifyRespondentTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/da-requested/da-requested.json";
    private static final String BASE_CASE_ERROR_RESPONSE = "fixtures/da-requested/da-requested-error.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenValidCaseData_whenNotifyRespondentOfDARequested_thenReturnDaRequestedByApplicantData() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, CcdCallbackRequest.class);
        Map<String, Object> response = cosApiClient.notifyRespondentOfDARequested(
                createCaseWorkerUser().getAuthToken(),
                ccdCallbackRequest);
        assertNotNull(response.get(DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseWithoutMandatoryField_whenNotifyRespondentOfDARequested_thenReturnError() {

        CcdCallbackRequest caseWithoutEmailAddress = ResourceLoader.loadJsonToObject(BASE_CASE_ERROR_RESPONSE, CcdCallbackRequest.class);
        Map<String, Object> response = cosApiClient.notifyRespondentOfDARequested(
                createCaseWorkerUser().getAuthToken(),
                caseWithoutEmailAddress);

        assertNull(response.get(DATA));
        List<String> error = (List<String>) response.get(ERRORS);
        assertEquals(1,error.size());
        assertTrue(error.get(0).contains("Could not evaluate value of mandatory property \"D8InferredPetitionerGender\""));
    }
}
