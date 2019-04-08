package uk.gov.hmcts.reform.divorce.util;

import java.time.format.DateTimeFormatter;

public class DateConstants {

    public static final String CCD_DATE_FORMAT = "yyyy-MM-dd";
    public static final DateTimeFormatter CCD_DATE_FORMATTER = DateTimeFormatter.ofPattern(CCD_DATE_FORMAT);

    private DateConstants() {
    }

}