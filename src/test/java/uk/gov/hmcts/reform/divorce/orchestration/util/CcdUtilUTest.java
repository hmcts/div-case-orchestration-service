package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DUE_DATE;

@RunWith(MockitoJUnitRunner.class)
public class CcdUtilUTest {

    private static final String CURRENT_DATE = "2018-01-01";
    private static final String PAYMENT_DATE = "01012018";
    private static final String EXPECTED_DATE_WITH_CUSTOMER_FORMAT = "01 January 2018";
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

    @Test
    public void whenGetDisplayCurrentDate_thenReturnExpectedDate() {
        assertThat(ccdUtil.getCurrentDateWithCustomerFacingFormat(), is(EXPECTED_DATE_WITH_CUSTOMER_FORMAT));
    }

    @Test
    public void givenDateStringInPast_whenIsCcdDateTimeInThePast_thenReturnTrue() {
        String pastDate = LocalDateTime.now(clock).minusMonths(1).toString();
        assertEquals(true, ccdUtil.isCcdDateTimeInThePast(pastDate));
    }

    @Test
    public void givenDateStringIsToday_whenIsCcdDateTimeInThePast_thenReturnTrue() {
        String now = LocalDateTime.now(clock).toString();
        assertEquals(true, ccdUtil.isCcdDateTimeInThePast(now));
    }

    @Test
    public void givenDateStringInFuture_whenIsCcdDateTimeInThePast_thenReturnFalse() {
        String futureDate = LocalDateTime.now(clock).plusMonths(1).toString();
        assertEquals(false, ccdUtil.isCcdDateTimeInThePast(futureDate));
    }

    @Test
    public void givenLocalDate_whenParseDecreeAbsoluteEligibleDate_thenReturnParsedDateString() {
        LocalDate hearingDate = LocalDate.of(2000, 1, 1);
        // 6 weeks and 1 day
        String expectedDate = "2000-02-13";

        assertEquals(expectedDate, ccdUtil.parseDecreeAbsoluteEligibleDate(hearingDate));
    }
}