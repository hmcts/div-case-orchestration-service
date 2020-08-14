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

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@Slf4j
public class ApplicationWithoutNoticeTest extends CcdSubmissionSupport {

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void givenServiceCentreCaseSubmitted_whenIssueEventFiredOnCCD_thenDocumentsAreGenerated() {

        CcdCallbackResponse response = cosApiClient.feeLookup(
            CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder().caseData(new HashMap<>()).build())
                .build()
        );

        assertNotNull(response);
        assertNull(response.getErrors());
        assertNull(response.getWarnings());
        assertFeeCodeIsCorrect(response);
        assertFeeAmountIsCorrect(response);
    }

    private static void assertFeeAmountIsCorrect(CcdCallbackResponse response) {
        assertThat(
            response.getData(),
            hasJsonPath(
                "$.generalApplicationWithoutNoticeFeeSummary.Fees[0].value.FeeAmount",
                is("5000")
            )
        );
    }

    private static void assertFeeCodeIsCorrect(CcdCallbackResponse response) {
        assertThat(
            response.getData(),
            hasJsonPath(
                "$.generalApplicationWithoutNoticeFeeSummary.Fees[0].value.FeeCode",
                is("FEE0228")
            ));
    }
}
