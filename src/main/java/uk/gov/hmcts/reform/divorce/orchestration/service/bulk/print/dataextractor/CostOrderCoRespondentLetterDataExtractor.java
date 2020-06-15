package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CostOrderCoRespondentLetterDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String SOLICITOR_REFERENCE = OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
    }

    public static String getHearingDate(Map<String, Object> caseData) throws TaskException {
        return getFormattedDate(caseData, DATETIME_OF_HEARING_CCD_FIELD);
    }

    public static String getLetterDate() {
        return DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now().toString());
    }

    private static String getFormattedDate(Map<String, Object> caseData, String dateProperty) {
        try {
            String date = getMandatoryPropertyValueAsString(caseData, dateProperty);
            return DateUtils.formatDateWithCustomerFacingFormat(date);
        } catch (TaskException e) {
            log.error("Date {} was in invalid format", dateProperty);
            throw new InvalidDataForTaskException(e);
        }
    }

    public static String getSolicitorReference(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CostOrderCoRespondentLetterDataExtractor.CaseDataKeys.SOLICITOR_REFERENCE);
    }
}
