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

public class AosOverdueNotifyPetitionerTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/aos-overdue/aos-overdue.json";
    private static final String BASE_CASE_ERROR_RESPONSE = "fixtures/aos-overdue/aos-overdue-error.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenValidCaseData_whenNotifyPetitionerOfAOSOverdue_thenReturnCallbackData() {
        CcdCallbackRequest ccdCallbackRequest = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, CcdCallbackRequest.class);
        Map<String, Object> response = cosApiClient.notifyPetitionerOfAOSOverdue(ccdCallbackRequest);
        assertNotNull(response.get(DATA));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseWithoutMandatoryField_whenNotifyPetitionerOfAOSOverdue_thenReturnError() {
        CcdCallbackRequest caseWithoutRelationshipInfo = ResourceLoader.loadJsonToObject(BASE_CASE_ERROR_RESPONSE, CcdCallbackRequest.class);
        Map<String, Object> response = cosApiClient.notifyPetitionerOfAOSOverdue(caseWithoutRelationshipInfo);

        assertNull(response.get(DATA));
        List<String> error = (List<String>) response.get(ERRORS);
        assertEquals(1,error.size());
        assertTrue(error.get(0).contains("Could not evaluate value of mandatory property \"D8DivorceWho\""));
    }
}
