package uk.gov.hmcts.reform.divorce.orchestration.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DateUtils {

    private static final DateTimeFormatter CLIENT_FACING_DATE_FORMAT = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.UK);

    public static String formatDateWithCustomerFacingFormat(LocalDate date) {
        return date.format(CLIENT_FACING_DATE_FORMAT);
    }

    public static String formatLetterDateFromDateTime(LocalDateTime dateTime) {
        return dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String formatLetterTimeFromDateTime(LocalDateTime dateTime) {
        return dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}