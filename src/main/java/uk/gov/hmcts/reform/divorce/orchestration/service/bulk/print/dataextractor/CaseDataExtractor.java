package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String CASE_REFERENCE = OrchestrationConstants.D_8_CASE_REFERENCE;
        public static final String PETITIONER_EMAIL = OrchestrationConstants.D_8_PETITIONER_EMAIL;
        public static final String PETITIONER_SOLICITOR_EMAIL = OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
    }

    public static String getCaseReference(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.CASE_REFERENCE);
    }

    public static String getPetitionerEmailOrEmpty(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.PETITIONER_EMAIL, "");
    }

    public static String getPetitionerEmail(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.PETITIONER_EMAIL);
    }

    public static String getPetitionerSolicitorEmail(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.PETITIONER_SOLICITOR_EMAIL);
    }
}
