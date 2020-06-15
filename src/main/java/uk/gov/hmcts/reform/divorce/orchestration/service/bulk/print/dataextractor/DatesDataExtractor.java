package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.Settings.ZONE_ID;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatesDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String DA_GRANTED_DATE = OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
    }

    public static String getHearingDate(Map<String, Object> caseData) {
        return DateUtils.formatDateWithCustomerFacingFormat(getHearingLocalDate(caseData));
    }

    public static String getDeadlineToContactCourtBy(Map<String, Object> caseData) {
        return formatDateWithCustomerFacingFormat(
            getHearingLocalDate(caseData).minus(PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT)
        );
    }

    public static String getLetterDate() {
        return formatDateWithCustomerFacingFormat(LocalDate.now(ZONE_ID));
    }

    public static String getDaGrantedDate(Map<String, Object> caseData) {
        return formatDateWithCustomerFacingFormat(getMandatoryStringValue(caseData, CaseDataKeys.DA_GRANTED_DATE));
    }

    private static LocalDate getHearingLocalDate(Map<String, Object> caseData) {
        try {
            return CaseDataUtils.getLatestCourtHearingDateFromCaseData(caseData);
        } catch (TaskException e) {
            log.error("Hearing date was invalid.");
            throw new InvalidDataForTaskException(e);
        }
    }
}
