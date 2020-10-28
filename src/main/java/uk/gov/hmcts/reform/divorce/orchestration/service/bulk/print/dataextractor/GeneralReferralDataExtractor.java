package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneralReferralDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String GENERAL_REFERRAL_FEE = CcdFields.GENERAL_REFERRAL_FEE;
        public static final String GENERAL_REFERRAL_DECISION_DATE = CcdFields.GENERAL_REFERRAL_DECISION_DATE;
        public static final String GENERAL_REFERRAL_REASON = CcdFields.GENERAL_REFERRAL_REASON;
        public static final String GENERAL_REFERRAL_TYPE = CcdFields.GENERAL_REFERRAL_TYPE;
        public static final String GENERAL_REFERRAL_DETAILS = CcdFields.GENERAL_REFERRAL_DETAILS;
        public static final String GENERAL_REFERRAL_PAYMENT_TYPE = CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE;
        public static final String GENERAL_REFERRAL_DECISION = CcdFields.GENERAL_REFERRAL_DECISION;
        public static final String GENERAL_REFERRAL_DECISION_REASON = CcdFields.GENERAL_REFERRAL_DECISION_REASON;
        public static final String GENERAL_APPLICATION_ADDED_DATE = CcdFields.GENERAL_APPLICATION_ADDED_DATE;
        public static final String GENERAL_APPLICATION_FROM = CcdFields.GENERAL_APPLICATION_FROM;
        public static final String GENERAL_APPLICATION_REFERRAL_DATE = CcdFields.GENERAL_APPLICATION_REFERRAL_DATE;
        public static final String ALTERNATIVE_SERVICE_MEDIUM = CcdFields.ALTERNATIVE_SERVICE_MEDIUM;
    }

    public static String getIsFeeRequired(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_FEE);
    }

    public static String getDecisionDateUnformatted(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_DECISION_DATE);
    }

    public static String getReason(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_REASON);
    }

    public static String getType(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_TYPE);
    }

    public static String getDetails(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_DETAILS);
    }

    public static String getPaymentType(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_PAYMENT_TYPE);
    }

    public static String getDecision(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_DECISION);
    }

    public static String getDecisionReason(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_DECISION_REASON);
    }

    public static String getApplicationAddedDateUnformatted(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_APPLICATION_ADDED_DATE);
    }

    public static String getApplicationFrom(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_APPLICATION_FROM);
    }

    public static String getApplicationReferralDateUnformatted(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.GENERAL_APPLICATION_REFERRAL_DATE, "");
    }

    public static String getAlternativeMedium(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.ALTERNATIVE_SERVICE_MEDIUM);
    }
}
