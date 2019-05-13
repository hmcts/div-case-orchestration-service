package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DUE_DATE;

public class CcdUtilUTest {
    private static final String CURRENT_DATE = "2018-01-01";
    private static final String EXPECTED_DATE = "2018-01-08";
    private static final String EXPECTED_DATE_WITH_CUSTOMER_FORMAT = "13 May 2019";
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

    @Test
    public void whenGiveDateAsYyyyMmDd_thenReturnFormattedDate() throws TaskException {

        Map<String, Object> testCaseData = new HashMap<>();
        testCaseData.put(CO_RESPONDENT_DUE_DATE, TEST_EXPECTED_DUE_DATE);

        assertEquals(TEST_EXPECTED_DUE_DATE_FORMATTED, CcdUtil.getFormattedDueDate(testCaseData, CO_RESPONDENT_DUE_DATE));
    }

    @Test
    public void whenGetDisplayCurrentDate_thenReturnExpectedDate() {
        assertThat(CcdUtil.getCurrentDateWithCustomerFacingFormat(), is(EXPECTED_DATE_WITH_CUSTOMER_FORMAT));
    }

}