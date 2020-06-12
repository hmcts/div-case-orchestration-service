package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.StringHelper.buildFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CostOrderCoRespondentLetterDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String CO_RESPONDENT_FIRST_NAME = OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
        public static final String CO_RESPONDENT_LAST_NAME = OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
        public static final String SOLICITOR_REFERENCE = OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
    }

    public static String getCoRespondentFullName(Map<String, Object> caseData) {
        return buildFullName(caseData, CaseDataKeys.CO_RESPONDENT_FIRST_NAME, CaseDataKeys.CO_RESPONDENT_LAST_NAME);
    }

    public static String getHearingDate(Map<String, Object> caseData) throws TaskException {
        return returnFormattedDate(caseData, DATETIME_OF_HEARING_CCD_FIELD);
    }

    public static String getLetterDate() {
        return DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now().toString());
    }

    public static String returnFormattedDate(Map<String, Object> caseData, String dateProperty)
        throws TaskException {
        return DateUtils.formatDateWithCustomerFacingFormat(getMandatoryPropertyValueAsString(caseData, dateProperty));
    }

    public static String getSolicitorReference(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CostOrderCoRespondentLetterDataExtractor.CaseDataKeys.SOLICITOR_REFERENCE);
    }
}
