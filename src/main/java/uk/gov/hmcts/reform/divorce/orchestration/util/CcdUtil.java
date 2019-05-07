package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_DATE_PATTERN;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@SuppressWarnings("squid:S1118")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdUtil {

    public static String mapCCDDateToDivorceDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(CCD_DATE_FORMAT))
                .format(DateTimeFormatter.ofPattern(PAYMENT_DATE_PATTERN));
    }

    public static String getFormattedDueDate(Map<String, Object> caseData, String dateToFormat) throws TaskException {
        String dateAsString = getMandatoryPropertyValueAsString(caseData, dateToFormat);
        LocalDate dueDate = LocalDate.parse(dateAsString);
        return DateUtils.formatDateWithCustomerFacingFormat(dueDate);
    }

}
