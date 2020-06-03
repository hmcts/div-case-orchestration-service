package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFormatter {
    public static String TEMPLATE_FORMAT = "dd MMM yyyy";

    public static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

        return date.format(formatter);
    }
}
