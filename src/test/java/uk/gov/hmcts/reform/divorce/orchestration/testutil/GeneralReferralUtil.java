package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

public class GeneralReferralUtil {

    public static Map<String, Object> buildCaseDataWithGeneralReferralFee(String referralFeeValue) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.GENERAL_REFERRAL_FEE, referralFeeValue);
        return caseData;
    }

    public static CcdCallbackRequest buildCallbackRequest(Map<String, Object> caseData, String caseState) {
        return CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(caseState)
                .caseData(caseData)
                .build())
            .build();
    }
}
