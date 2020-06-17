package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CcdDateUtilsTest {

    @Test
    public void shouldConvertDateFromCCDFormat() {
        String formattedDate = CcdUtil.formatFromCCDFormatToHumanReadableFormat("2017-08-15");
        assertThat(formattedDate, is("15/08/2017"));
    }

}