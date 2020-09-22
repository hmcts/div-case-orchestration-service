package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String PETITIONER_EMAIL = CcdFields.PETITIONER_EMAIL;
        public static final String PETITIONER_SOLICITOR_EMAIL = OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
        public static final String RESPONDENT_EMAIL = OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
        public static final String RESPONDENT_SOLICITOR_EMAIL = OrchestrationConstants.RESPONDENT_SOLICITOR_EMAIL_ADDRESS;
        public static final String CO_RESPONDENT_EMAIL = OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
        public static final String CO_RESPONDENT_SOLICITOR_EMAIL = CcdFields.CO_RESPONDENT_SOLICITOR_EMAIL_ADDRESS;
        public static final String OTHER_PARTY_EMAIL = CcdFields.GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL;
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

    public static String getRespondentEmail(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.RESPONDENT_EMAIL);
    }

    public static String getRespondentSolicitorEmail(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.RESPONDENT_SOLICITOR_EMAIL);
    }

    public static String getCoRespondentEmail(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.CO_RESPONDENT_EMAIL);
    }

    public static String getCoRespondentSolicitorEmail(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.CO_RESPONDENT_SOLICITOR_EMAIL);
    }

    public static String getOtherPartyEmail(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.OTHER_PARTY_EMAIL);
    }
}
