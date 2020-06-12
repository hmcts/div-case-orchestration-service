package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;

import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.StringHelper.buildFullName;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FullNamesDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String PETITIONER_FIRST_NAME = OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
        public static final String PETITIONER_LAST_NAME = OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
        public static final String PETITIONER_SOLICITOR_NAME = OrchestrationConstants.PET_SOL_NAME;
        public static final String RESPONDENT_FIRST_NAME = OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
        public static final String RESPONDENT_LAST_NAME = OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
        public static final String RESPONDENT_SOLICITOR_NAME = OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
        public static final String CO_RESPONDENT_FIRST_NAME = OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
        public static final String CO_RESPONDENT_LAST_NAME = OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
        public static final String CO_RESPONDENT_SOLICITOR_NAME = "CoRespondentSolicitorName";
    }

    public static String getPetitionerFullName(Map<String, Object> caseData) {
        return buildFullName(caseData, CaseDataKeys.PETITIONER_FIRST_NAME, CaseDataKeys.PETITIONER_LAST_NAME);
    }

    public static String getPetitionerSolicitorFullName(Map<String, Object> caseData) {
        return nullToEmpty((String) (caseData.get(CaseDataKeys.PETITIONER_SOLICITOR_NAME))).trim();
    }

    public static String getRespondentFullName(Map<String, Object> caseData) {
        return buildFullName(caseData, CaseDataKeys.RESPONDENT_FIRST_NAME, CaseDataKeys.RESPONDENT_LAST_NAME);
    }

    public static String getRespondentSolicitorFullName(Map<String, Object> caseData) {
        return nullToEmpty((String) (caseData.get(CaseDataKeys.RESPONDENT_SOLICITOR_NAME))).trim();
    }

    public static String getCoRespondentFullName(Map<String, Object> caseData) {
        return buildFullName(caseData, CaseDataKeys.CO_RESPONDENT_FIRST_NAME, CaseDataKeys.CO_RESPONDENT_LAST_NAME);
    }

    public static String getCoRespondentSolicitorFullName(Map<String, Object> caseData) {
        return nullToEmpty((String) (caseData.get(CaseDataKeys.CO_RESPONDENT_SOLICITOR_NAME))).trim();
    }
}
