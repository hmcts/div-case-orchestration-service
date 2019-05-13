package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DUE_DATE;

@RunWith(MockitoJUnitRunner.class)
public class CcdUtilUTest {

    private static final String CURRENT_DATE = "2018-01-01";
    private static final String PAYMENT_DATE = "01012018";
    private static final LocalDateTime FIXED_DATE_TIME = LocalDate.parse(CURRENT_DATE).atStartOfDay();

    @InjectMocks
    private CcdUtil ccdUtil;

    @Mock
    private Clock clock;

    @Before
    public void before() {
        when(clock.instant()).thenReturn(FIXED_DATE_TIME.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(UTC);
    }

    @Test
    public void testConstructorPrivate() throws Exception {
        Constructor<CcdUtil> constructor = CcdUtil.class.getDeclaredConstructor(Clock.class);
        assertTrue(Modifier.isPublic(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance(clock);
    }

    @Test
    public void whenGetCurrentDate_thenReturnExpectedDate() {
        assertEquals(CURRENT_DATE, ccdUtil.getCurrentDateCcdFormat());
    }

    @Test
    public void whenCurrentDatePaymentPattern_thenReturnExpectedDate() {
        assertEquals(PAYMENT_DATE, ccdUtil.getCurrentDatePaymentFormat());
    }

    @Test
    public void whenGiveDateAsYyyyMmDd_thenReturnFormattedDate() throws TaskException {

        Map<String, Object> testCaseData = new HashMap<>();
        testCaseData.put(CO_RESPONDENT_DUE_DATE, TEST_EXPECTED_DUE_DATE);

        assertEquals(TEST_EXPECTED_DUE_DATE_FORMATTED, ccdUtil.getFormattedDueDate(testCaseData, CO_RESPONDENT_DUE_DATE));
    }

}