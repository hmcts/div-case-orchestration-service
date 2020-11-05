package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateCalculator {

    public static String getDateWithOffset(int offset) {
        return DateUtils.formatDateFromLocalDate(LocalDate.now().plus(offset, ChronoUnit.DAYS));
    }

}
