package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SolicitorDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String SOLICITOR_REFERENCE = OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
        public static final String SOLICITOR_PAYMENT_METHOD = OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
    }

    public static String getSolicitorReference(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.SOLICITOR_REFERENCE, "");
    }

    public static String getPaymentMethod(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.SOLICITOR_PAYMENT_METHOD, "");
    }
}
