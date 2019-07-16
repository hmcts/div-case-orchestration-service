package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.time.LocalDate;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_RESPONDENT_ELIGIBLE_FOR_DA_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.util.DateConstants.CCD_DATE_FORMATTER;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class ProcessApplicantDAEligibilityCallbackTest extends IntegrationTest {

    private static final String BASIC_CASE = "fixtures/callback/basic-case.json";
    private static final String FAMILY_MAN_REFERENCE_JSON_PATH = "$.data.D8caseReference";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void testCallbackFor_ProcessingApplicantDAEligibility() {
        CcdCallbackRequest basicCcdCallbackRequest = ResourceLoader.loadJsonToObject(BASIC_CASE, CcdCallbackRequest.class);
        Map<String, Object> inputCaseData = basicCcdCallbackRequest.getCaseDetails().getCaseData();
        inputCaseData.put(DECREE_NISI_GRANTED_DATE_CCD_FIELD, LocalDate.now().format(CCD_DATE_FORMATTER));

        CcdCallbackResponse response = cosApiClient.processApplicantDAEligibility(basicCcdCallbackRequest);

        JSONAssert.assertEquals(objectToJson(inputCaseData), objectToJson(response.getData()), LENIENT);
        assertThat(response.getData(), allOf(
            hasKey(DATE_RESPONDENT_ELIGIBLE_FOR_DA_CCD_FIELD),
            hasKey(DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA_CCD_FIELD)
        ));
        assertThat(response.getErrors(), is(nullValue()));
        assertThat(response.getWarnings(), is(nullValue()));
        String jsonResponse = objectToJson(response);
        assertThat(jsonResponse, hasJsonPath(FAMILY_MAN_REFERENCE_JSON_PATH, is("LV17D80100")));
    }

}