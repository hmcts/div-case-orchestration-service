package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DateFormatterTest {

    @Test
    public void formatDateReturnValidDates() {
        assertThat(DateFormatter.formatDate(LocalDate.of(2000, 10, 10)), is("10 Oct 2000"));
        assertThat(DateFormatter.formatDate(LocalDate.of(2000, 5, 1)), is("01 May 2000"));
        assertThat(DateFormatter.formatDate(LocalDate.of(100, 5, 1)), is("01 May 0100"));
    }
}
