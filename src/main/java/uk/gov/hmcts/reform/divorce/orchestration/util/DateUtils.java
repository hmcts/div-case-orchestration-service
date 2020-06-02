package uk.gov.hmcts.reform.divorce.orchestration.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static String TEMPLATE_FORMAT = "dd MMM yyyy";

    public static String formatDate(LocalDate date, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        return date.format(formatter);
    }

    public static LocalDate todaysDate() {
        return LocalDate.now(ZoneId.of("Europe/London"));
    }
}