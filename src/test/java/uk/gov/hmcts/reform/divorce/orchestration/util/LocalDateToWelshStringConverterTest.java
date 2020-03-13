package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LocalDateToWelshStringConverterTest {
    @Autowired
    private LocalDateToWelshStringConverter localDateToWelshStringConverter;

    @Test
    public void testLocalDateConvertedToWelsh() {
        LocalDate localDate = LocalDate.of(2020, 01, 27);
        String novMonth = localDateToWelshStringConverter.convert(localDate);
        Assert.assertEquals("27 Ionawr 2020", novMonth);
    }

}
