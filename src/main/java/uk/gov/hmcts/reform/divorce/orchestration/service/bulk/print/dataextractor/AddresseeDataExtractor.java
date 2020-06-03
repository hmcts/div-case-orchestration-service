package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddresseeDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String RESPONDENT_CORRESPONDENCE_ADDRESS = "D8DerivedRespondentCorrespondenceAddr";
        public static final String RESPONDENT_SOLICITOR_ADDRESS = "D8DerivedRespondentSolicitorAddr";
    }

    public static Addressee getRespondent(Map<String, Object> caseData) {
        return Addressee.builder()
            .name(getRespondentFullName(caseData))
            .formattedAddress(getRespondentFormattedAddress(caseData))
            .build();
    }

    public static Addressee getRespondentSolicitor(Map<String, Object> caseData) {
        return Addressee.builder()
            .name(getRespondentSolicitorFullName(caseData))
            .formattedAddress(getRespondentSolicitorFormattedAddress(caseData))
            .build();
    }

    private static String getRespondentFormattedAddress(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.RESPONDENT_CORRESPONDENCE_ADDRESS);
    }

    private static String getRespondentSolicitorFormattedAddress(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.RESPONDENT_SOLICITOR_ADDRESS);
    }

    private static String getMandatoryStringValue(Map<String, Object> caseData, String field) {
        try {
            return getMandatoryPropertyValueAsString(caseData, field);
        } catch (TaskException exception) {
            throw new InvalidDataForTaskException(exception);
        }
    }
}
