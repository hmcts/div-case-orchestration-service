package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceApplicationDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String REFUSAL_REASON = CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
        public static final String SERVICE_APPLICATION_GRANTED = CcdFields.SERVICE_APPLICATION_GRANTED;
        public static final String SERVICE_APPLICATION_TYPE = CcdFields.SERVICE_APPLICATION_TYPE;
        public static final String SERVICE_APPLICATION_PAYMENT = CcdFields.SERVICE_APPLICATION_PAYMENT;
    }

    public static String getServiceApplicationRefusalReason(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.REFUSAL_REASON);
    }

    public static String getServiceApplicationRefusalReasonOrEmpty(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.REFUSAL_REASON, "");
    }

    public static String getServiceApplicationGranted(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.SERVICE_APPLICATION_GRANTED);
    }

    public static String getServiceApplicationType(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.SERVICE_APPLICATION_TYPE);
    }

    public static String getServiceApplicationPayment(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.SERVICE_APPLICATION_PAYMENT);
    }
}
