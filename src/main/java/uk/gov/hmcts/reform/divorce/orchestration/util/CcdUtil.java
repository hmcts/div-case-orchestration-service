package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_DATE_PATTERN;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@SuppressWarnings("squid:S1118")
@AllArgsConstructor
@Component
public class CcdUtil {

    private final Clock clock;

    public String getCurrentDateCcdFormat() {
        return LocalDate.now(clock).format(DateTimeFormatter.ofPattern(CCD_DATE_FORMAT));
    }

    public String getCurrentDatePaymentFormat() {
        return LocalDate.now(clock).format(DateTimeFormatter.ofPattern(PAYMENT_DATE_PATTERN));
    }

    public String mapCCDDateToDivorceDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(CCD_DATE_FORMAT))
            .format(DateTimeFormatter.ofPattern(PAYMENT_DATE_PATTERN));
    }

    public static String mapDivorceDateTimeToCCDDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    public String getCurrentDateWithCustomerFacingFormat() {
        return DateUtils.formatDateWithCustomerFacingFormat(java.time.LocalDate.now(clock));
    }

    public String getFormattedDueDate(Map<String, Object> caseData, String dateToFormat) throws TaskException {
        String dateAsString = getMandatoryPropertyValueAsString(caseData, dateToFormat);
        LocalDate dueDate = LocalDate.parse(dateAsString);
        return DateUtils.formatDateWithCustomerFacingFormat(dueDate);
    }

    public boolean isCcdDateTimeInThePast(String date) {
        return LocalDateTime.parse(date).toLocalDate().isBefore(LocalDate.now(clock).plusDays(1));
    }

    public String parseDecreeAbsoluteEligibleDate(LocalDate grantedDate) {
        return DateUtils.formatDateFromLocalDate(
            grantedDate.plusWeeks(6).plusDays(1)
        );
    }

    public static LocalDate parseDateUsingCcdFormat(String date) {
        return LocalDate.parse(date, ofPattern(CCD_DATE_FORMAT));
    }

    public static String formatDateForCCD(LocalDate plus) {
        return plus.format(ofPattern(CCD_DATE_FORMAT));
    }

}