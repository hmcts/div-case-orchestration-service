package uk.gov.hmcts.reform.divorce.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_ANSWERS_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class AosReceivedCallbackTest extends IntegrationTest {

    private static final String BASE_CASE_RESPONSE = "fixtures/retrieve-aos-case/aos-received.json";
    private static final String ERROR_CASE_RESPONSE = "fixtures/retrieve-aos-case/aos-received-error.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenCase_whenSubmitAOS_thenReturnAOSDataPlusAnswersDocument() {
        Map<String, Object> aosCase = ResourceLoader.loadJsonToObject(BASE_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient.aosReceived(createCaseWorkerUser().getAuthToken(), aosCase);
        Map<String, Object> resData = (Map<String, Object>) response.get(DATA);
        String caseRefResponse = (String) resData.get(D_8_CASE_REFERENCE);
        Map<String, Object> caseData = (Map<String, Object>) ((Map<String, Object>)aosCase.get(CASE_DETAILS)).get(CASE_DATA);
        String json = objectToJson(response);
        assertEquals(caseData.get(D_8_CASE_REFERENCE), caseRefResponse);
        assertNotNull(resData);
        assertThat(json, hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentFileName",
            is(RESPONDENT_ANSWERS_TEMPLATE_NAME)));
        assertThat(json, hasJsonPath("$.data.D8caseReference",
            is("LV17D80100")));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseWithoutEmail_whenSubmitAOS_thenReturnNotificationError() {

        Map<String, Object> aosCaseWithoutEmailAddress = ResourceLoader
                .loadJsonToObject(ERROR_CASE_RESPONSE, Map.class);
        Map<String, Object> response = cosApiClient
                .aosReceived(createCaseWorkerUser().getAuthToken(), aosCaseWithoutEmailAddress);

        assertNull(response.get(DATA));
        List<String> error = (List<String>) response.get(ERRORS);
        assertEquals(1,error.size());
        assertTrue(error.get(0).contains("email_address is a required property"));
    }
}
