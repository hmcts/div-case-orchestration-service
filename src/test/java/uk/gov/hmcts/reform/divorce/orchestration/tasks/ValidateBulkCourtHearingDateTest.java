package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE;

@RunWith(MockitoJUnitRunner.class)
public class ValidateBulkCourtHearingDateTest {
    private TaskContext taskContext;

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private ValidateBulkCourtHearingDate classToTest;

    @Before
    public void setup() {
        taskContext = new DefaultTaskContext();
    }

    @Test
    public void givenFutureCourtHearingDate_whenValidateCourtHearingDate_thenReturnCaseDetails() throws TaskException {
        String futureDate = LocalDateTime.now().plusMonths(3).toString();
        Map<String, Object> bulkCaseData = Collections.singletonMap(COURT_HEARING_DATE, futureDate);

        when(ccdUtil.isCcdDateTimeInThePast(futureDate)).thenReturn(false);

        assertEquals(bulkCaseData, classToTest.execute(taskContext, bulkCaseData));
    }

    @Test(expected = TaskException.class)
    public void givenPastCourtHearingDate_whenValidateCourtHearingDate_thenThrowTaskException() throws TaskException {
        String pastDate = LocalDateTime.now().minusMonths(3).toString();
        Map<String, Object> bulkCaseData = Collections.singletonMap(COURT_HEARING_DATE, pastDate);

        when(ccdUtil.isCcdDateTimeInThePast(pastDate)).thenReturn(true);

        classToTest.execute(taskContext, bulkCaseData);
    }

    @Test(expected = TaskException.class)
    public void givenSameDayCourtHearingDate_whenValidateCourtHearingDate_thenThrowTaskException() throws TaskException {
        String todaysDate = LocalDateTime.now().toString();
        Map<String, Object> bulkCaseData = Collections.singletonMap(COURT_HEARING_DATE, todaysDate);

        when(ccdUtil.isCcdDateTimeInThePast(todaysDate)).thenReturn(true);

        classToTest.execute(taskContext, bulkCaseData);
    }
}
