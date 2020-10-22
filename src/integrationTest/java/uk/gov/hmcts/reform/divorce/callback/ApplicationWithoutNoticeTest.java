package uk.gov.hmcts.reform.divorce.callback;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@Slf4j
public class ApplicationWithoutNoticeTest extends CcdSubmissionSupport {

    public static final String EXPECTED_AMOUNT = "5000";
    public static final String EXPECTED_FEE_CODE = "FEE0228";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenCase_whenServicePaymentIsTriggered_ApplicationWithoutNoticeFeeSummaryOrderIsReturned() {

        CcdCallbackResponse response = cosApiClient.setupConfirmServicePayment(
            CcdCallbackRequest.builder()
                .caseDetails(
                    CaseDetails.builder()
                        .caseData(new HashMap<>())
                        .caseId(TEST_CASE_ID)
                        .build()
                ).build()
        );

        assertNotNull(response);
        assertNull(response.getErrors());
        assertNull(response.getWarnings());
        assertFeeCodeIsCorrect(response.getData());
        assertFeeAmountIsCorrect(response.getData());
    }

    private static void assertFeeCodeIsCorrect(Map<String, Object> caseData) {
        assertValue(caseData, "FeeCode", EXPECTED_FEE_CODE);
    }

    private static void assertFeeAmountIsCorrect(Map<String, Object> caseData) {
        assertValue(caseData, "FeeAmount", EXPECTED_AMOUNT);
    }

    private static void assertValue(Map<String, Object> caseData, String field, String value) {
        String jsonPath = format("$.generalApplicationWithoutNoticeFeeSummary.Fees[0].value.%s", field);
        assertThat(caseData, hasJsonPath(jsonPath, is(value)));
    }
}
