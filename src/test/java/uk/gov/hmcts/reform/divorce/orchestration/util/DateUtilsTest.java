package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils.TEMPLATE_FORMAT;

public class DateUtilsTest {

    @Test
    public void formatDateReturnValidDates() {
        assertThat(DateUtils.formatDate(LocalDate.of(2000, 10, 10), TEMPLATE_FORMAT), is("10 Oct 2000"));
        assertThat(DateUtils.formatDate(LocalDate.of(2000, 5, 1), TEMPLATE_FORMAT), is("01 May 2000"));
        assertThat(DateUtils.formatDate(LocalDate.of(100, 5, 1), TEMPLATE_FORMAT), is("01 May 0100"));
    }
}
