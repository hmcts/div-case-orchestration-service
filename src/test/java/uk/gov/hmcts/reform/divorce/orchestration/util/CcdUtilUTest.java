package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CcdUtilUTest {

    @Test
    public void testConstructorPrivate() throws Exception {
        Constructor<CcdUtil> constructor = CcdUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void whenGetCurrentDate_thenReturnExpectedDate() {
        final LocalDate localDate = LocalDate.now();
        final String expectedDate = String.format("%d-%2d-%2d", localDate.getYear(), localDate.getMonthValue(),
            localDate.getDayOfMonth());

        assertEquals(expectedDate, CcdUtil.getCurrentDate());
    }

}