package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@SuppressWarnings("squid:S1118")
@AllArgsConstructor
@Component
public class CcdUtil {

    private static final String UK_HUMAN_READABLE_DATE_FORMAT = "dd/MM/yyyy";
    private static final String PAYMENT_DATE_PATTERN = "ddMMyyyy";

    private final Clock clock;

    public String getCurrentDateCcdFormat() {
        return LocalDate.now(clock).format(DateUtils.Formatters.CCD_DATE);
    }

    public String getCurrentDatePaymentFormat() {
        return LocalDate.now(clock).format(DateTimeFormatter.ofPattern(PAYMENT_DATE_PATTERN, DateUtils.Settings.LOCALE));
    }

    public String mapCCDDateToDivorceDate(String date) {
        return LocalDate.parse(date, DateUtils.Formatters.CCD_DATE)
            .format(DateTimeFormatter.ofPattern(PAYMENT_DATE_PATTERN, DateUtils.Settings.LOCALE));
    }

    public static String mapDivorceDateTimeToCCDDateTime(LocalDateTime dateTime) {
        return DateUtils.formatDateTimeForCcd(dateTime);
    }

    public static LocalDateTime mapCCDDateTimeToLocalDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime);
    }

    public String getCurrentDateWithCustomerFacingFormat() {
        return DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now(clock));
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
        return LocalDate.parse(date, DateUtils.Formatters.CCD_DATE);
    }

    public static String formatDateForCCD(LocalDate plus) {
        return plus.format(DateUtils.Formatters.CCD_DATE);
    }

    public static String formatFromCCDFormatToHumanReadableFormat(String inputDate) {
        LocalDate localDate = parseDateUsingCcdFormat(inputDate);
        return localDate.format(DateTimeFormatter.ofPattern(UK_HUMAN_READABLE_DATE_FORMAT, DateUtils.Settings.LOCALE));
    }

    public static String retrieveAndFormatCCDDateFieldIfPresent(String fieldName, Map<String, Object> caseData, String defaultValue) {
        return Optional.ofNullable(caseData.get(fieldName))
            .map((String.class::cast))
            .map(CcdUtil::formatFromCCDFormatToHumanReadableFormat)
            .orElse(defaultValue);
    }

    public LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now(clock);
    }
}
