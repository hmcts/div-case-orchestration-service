package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneralEmailCaseDataExtractor extends Extractable {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String GENERAL_EMAIL_DETAILS = CcdFields.GENERAL_EMAIL_DETAILS;
    }

    public static String getGeneralEmailDetails(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_EMAIL_DETAILS);
    }

}