package uk.gov.hmcts.reform.divorce.orchestration.util;

import java.time.format.DateTimeFormatter;

import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.parseDateUsingCcdFormat;

public class DateUtils {

    public static final String UK_HUMAN_READABLE_DATE_FORMAT = "dd/MM/yyyy";

    public static String formatFromCCDFormatToHumanReadableFormat(String inputDate) {
        return parseDateUsingCcdFormat(inputDate).format(DateTimeFormatter.ofPattern(UK_HUMAN_READABLE_DATE_FORMAT));
    }

}