package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.FEE_ACCOUNT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.HELP_WITH_FEE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FurtherPaymentsHelper {

    public static String getGeneralReferralPaymentType() {
        return CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE;
    }

    public static String getServiceApplicationPaymentType() {
        return CcdFields.SERVICE_APPLICATION_PAYMENT;
    }

    public static String getPaymentType(Map<String, Object> caseData, String ccdPaymentTypeField) {
        return Optional.ofNullable((String) caseData.get(ccdPaymentTypeField))
            .orElseGet(() -> EMPTY_STRING);
    }

    public static boolean isHelpWithFeePayment(String paymentType) {
        return HELP_WITH_FEE_TYPE.equals(paymentType);
    }

    public static boolean isFeeAccountPayment(String paymentType) {
        return FEE_ACCOUNT_TYPE.equals(paymentType);
    }
}
