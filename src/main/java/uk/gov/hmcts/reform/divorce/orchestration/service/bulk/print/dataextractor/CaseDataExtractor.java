package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String CASE_REFERENCE = OrchestrationConstants.D_8_CASE_REFERENCE;

    }

    public static String getCaseReference(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.CASE_REFERENCE);
    }

    public static String getServiceApplicationRefusalReason(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CcdFields.SERVICE_APPLICATION_REFUSAL_REASON);
    }

    public static String getServiceApplicationGranted(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CcdFields.SERVICE_APPLICATION_GRANTED);
    }

    public static String getServiceApplicationType(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CcdFields.SERVICE_APPLICATION_TYPE);
    }

}
