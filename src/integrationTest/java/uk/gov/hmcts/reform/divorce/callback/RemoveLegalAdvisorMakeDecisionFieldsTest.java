package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class RemoveLegalAdvisorMakeDecisionFieldsTest extends IntegrationTest {

    private static final String CASE_DATA = "fixtures/dn-outcome/generic-dn-outcome-case.json";

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenCase_whenRemoveDnOutcomeCaseFlag_thenReturnCaseDataWithNoDnOutcomeCaseFlag() {
        CcdCallbackRequest dnOutcomeCase = ResourceLoader.loadJsonToObject(CASE_DATA, CcdCallbackRequest.class);
        Map<String, Object> response = cosApiClient.removeLegalAdvisorMakeDecisionFields(dnOutcomeCase);
        Map<String, Object> resData = (Map<String, Object>) response.get(DATA);
        String jsonResponse = objectToJson(response);
        assertNotNull(resData);
        assertThat(jsonResponse, hasNoJsonPath("$.data.DecreeNisiGranted"));
        assertThat(jsonResponse, hasNoJsonPath("$.data.CostsClaimGranted"));
        assertThat(jsonResponse, hasNoJsonPath("$.data.WhoPaysCosts"));
        assertThat(jsonResponse, hasNoJsonPath("$.data.TypeCostsDecision"));
        assertThat(jsonResponse, hasNoJsonPath("$.data.CostsOrderAdditionalInfo"));
        assertThat(jsonResponse, hasJsonPath("$.data.D8caseReference",
                is("LV80D85000")));
    }
}
