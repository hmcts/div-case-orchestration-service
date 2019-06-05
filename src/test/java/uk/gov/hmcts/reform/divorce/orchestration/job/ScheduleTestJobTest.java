package uk.gov.hmcts.reform.divorce.orchestration.job;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScheduleTestJobTest {

    @Mock
    private JobExecutionContext jobExecutionContextMock;

    @Mock
    private Scheduler schedulerMock;

    @InjectMocks
    private ScheduleTestJob classToTest;

    @Before
    public void setUp() {
        when(jobExecutionContextMock.getScheduler()).thenReturn(schedulerMock);
    }

    @Test
    public void whenExecute_thenNoError() {
        classToTest.execute(jobExecutionContextMock);
    }
}
