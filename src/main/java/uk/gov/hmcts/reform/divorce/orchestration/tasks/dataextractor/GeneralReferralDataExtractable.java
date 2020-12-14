package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor;

import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceGeneralReferral;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getListOfCollectionMembers;

public interface GeneralReferralDataExtractable extends Extractable {

    class CaseDataKeys {
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

    default String getIsFeeRequired(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_FEE);
    }

    default String getDecisionDateUnformatted(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_DECISION_DATE);
    }

    default String getReason(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_REASON);
    }

    default String getType(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_TYPE);
    }

    default String getDetails(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_DETAILS);
    }

    default String getPaymentType(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_PAYMENT_TYPE);
    }

    default String getDecision(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_DECISION);
    }

    default String getDecisionReason(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_REFERRAL_DECISION_REASON);
    }

    default String getApplicationAddedDateUnformatted(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_APPLICATION_ADDED_DATE);
    }

    default String getApplicationFrom(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_APPLICATION_FROM);
    }

    default String getApplicationReferralDateUnformatted(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.GENERAL_APPLICATION_REFERRAL_DATE, "");
    }

    default String getAlternativeMedium(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.ALTERNATIVE_SERVICE_MEDIUM);
    }

    default List<CollectionMember<DivorceGeneralReferral>> getListOfGeneralReferrals(Map<String, Object> caseData) {
        return getListOfCollectionMembers(CcdFields.GENERAL_REFERRALS, caseData);
    }
}
