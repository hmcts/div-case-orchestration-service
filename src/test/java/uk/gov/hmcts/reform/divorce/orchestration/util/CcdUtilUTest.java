package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CcdUtilUTest {
    private static final String CURRENT_DATE = "2018-01-01";
    private static final String EXPECTED_DATE = "2018-01-08";
    private static final int DAYS_OFFSET = 7;

    @Before
    public void setup() {
        DateTimeUtils.setCurrentMillisFixed(new org.joda.time.LocalDate(CURRENT_DATE).toDate().getTime());
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void testConstructorPrivate() throws Exception {
        Constructor<CcdUtil> constructor = CcdUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void whenGetCurrentDate_thenReturnExpectedDate() {
        assertEquals(CURRENT_DATE, CcdUtil.getCurrentDate());
    }

    @Test
    public void whenGetCurrentDatePlusDays_thenReturnExpectedDate() {
        assertEquals(EXPECTED_DATE, CcdUtil.getCurrentDatePlusDays(DAYS_OFFSET));
    }
}