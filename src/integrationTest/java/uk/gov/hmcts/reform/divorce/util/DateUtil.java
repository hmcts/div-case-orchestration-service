package uk.gov.hmcts.reform.divorce.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtil {

    private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static String parseCurrentDate() {
        return DATE_ONLY_FORMAT.format(new Date());
    }

}
