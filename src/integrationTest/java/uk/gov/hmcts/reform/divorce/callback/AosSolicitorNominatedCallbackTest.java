package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AosSolicitorNominatedCallbackTest extends IntegrationTest {
    private static final String BASE_CASE_RESPONSE = "fixtures/respond-to-a-divorce/ccd-callback-aos-solicitor-nominated.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void whenSubmitAOSSubmittedIsCalledBack_thenReturnAOSData() {
        //given
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);

        //when
        Map<String, Object> response = cosApiClient.aosSolicitorNominated(aosCase);

        //then
        Map<String, Object> caseDetails = (Map<String, Object>) aosCase.get(CASE_DETAILS);
        Map<String, Object> expectedCaseData = new HashMap<>((Map<String, Object>) caseDetails.get(CASE_DATA));
        expectedCaseData.put("ReceivedAOSfromResp", null);
        expectedCaseData.put("ReceivedAOSfromRespDate", null);
        expectedCaseData.put("RespEmailAddress", null);

        Map<String, String> responseData = (Map<String, String>)response.get(DATA);
        assertNotNull(responseData);
        assertNotNull(responseData.get("AosLetterHolderId"));
        responseData.remove("AosLetterHolderId"); //remove dynamic field to assert expected response
        assertEquals(expectedCaseData, responseData);
    }
}
