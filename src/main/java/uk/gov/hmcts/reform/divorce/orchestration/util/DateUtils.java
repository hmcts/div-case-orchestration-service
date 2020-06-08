package uk.gov.hmcts.reform.divorce.orchestration.util;

import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtils {

    public static LocalDate todaysDate() {
        return LocalDate.now(ZoneId.of("Europe/London"));
    }
}