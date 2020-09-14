package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneralEmailCaseDataExtractor {

    public static String getGeneralEmailDetails(Map<String, Object> caseData) {
        return getMandatoryPropertyValueAsString(caseData, GENERAL_EMAIL_DETAILS);
    }
}
