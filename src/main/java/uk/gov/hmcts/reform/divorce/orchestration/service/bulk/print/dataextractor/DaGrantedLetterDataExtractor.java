package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.StringHelper.buildFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DaGrantedLetterDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String DA_GRANTED_DATE = OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
        public static final String PETITIONER_FIRST_NAME = OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
        public static final String PETITIONER_LAST_NAME = OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
        public static final String RESPONDENT_FIRST_NAME = OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
        public static final String RESPONDENT_LAST_NAME = OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
        public static final String DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS = "D8DerivedRespondentCorrespondenceAddr";
    }

    public static String getDaGrantedDate(Map<String, Object> caseData) throws TaskException {
        return getMandatoryPropertyValueAsString(caseData, CaseDataKeys.DA_GRANTED_DATE);
    }

    public static Addressee getAddressee(Map<String, Object> caseData) throws InvalidDataForTaskException {
        return Addressee.builder()
            .name(getRespondentFullName(caseData))
            .formattedAddress(formatAddressForLetterPrinting(caseData))
            .build();
    }

    public static String getRespondentFullName(Map<String, Object> caseData) {
        return buildFullName(caseData, CaseDataKeys.RESPONDENT_FIRST_NAME, CaseDataKeys.RESPONDENT_LAST_NAME);
    }

    public static String getPetitionerFullName(Map<String, Object> caseData) {
        return buildFullName(caseData, CaseDataKeys.PETITIONER_FIRST_NAME, CaseDataKeys.PETITIONER_LAST_NAME);
    }

    private static String formatAddressForLetterPrinting(Map<String, Object> caseData) throws InvalidDataForTaskException {
        String addressLines = null;

        try {
            addressLines = getMandatoryPropertyValueAsString(caseData, CaseDataKeys.DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS);
        } catch (TaskException exception) {
            throw new InvalidDataForTaskException(exception);
        }

        return addressLines;
    }
}
