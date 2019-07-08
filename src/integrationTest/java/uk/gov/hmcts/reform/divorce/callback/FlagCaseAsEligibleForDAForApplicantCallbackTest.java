package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class FlagCaseAsEligibleForDAForApplicantCallbackTest extends IntegrationTest {

    private static final String BASIC_CASE = "fixtures/callback/basic-case.json";
    private static final String FAMILY_MAN_REFERENCE_JSON_PATH = "$.data.D8caseReference";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCase_whenSubmitAOS_thenReturnAOSDataPlusAnswersDocument() {
        CcdCallbackRequest basicCcdCallbackRequest = ResourceLoader.loadJsonToObject(BASIC_CASE, CcdCallbackRequest.class);

        CcdCallbackResponse response = cosApiClient.flagCaseAsEligibleForDAForApplicant(basicCcdCallbackRequest);

        Map<String, Object> inputCaseData = basicCcdCallbackRequest.getCaseDetails().getCaseData();
        assertThat(response.getData(), equalTo(inputCaseData));
        assertThat(response.getErrors(), is(nullValue()));
        assertThat(response.getWarnings(), is(nullValue()));
        String jsonResponse = objectToJson(response);
        assertThat(jsonResponse, hasJsonPath(FAMILY_MAN_REFERENCE_JSON_PATH, is("LV17D80100")));
    }

}