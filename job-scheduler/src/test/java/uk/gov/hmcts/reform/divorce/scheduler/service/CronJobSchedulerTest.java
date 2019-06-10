package uk.gov.hmcts.reform.divorce.scheduler.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobKey;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.scheduler.config.SchedulerConfig;
import uk.gov.hmcts.reform.divorce.scheduler.exception.JobException;
import uk.gov.hmcts.reform.divorce.scheduler.model.Schedule;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CronJobSchedulerTest {

    @Mock
    private SchedulerConfig schedulerConfig;

    @Mock
    private JobService jobService;

    @InjectMocks
    private CronJobScheduler classToTest;

    @Test
    public void whenReCreateScheduleIsFalse_thenDoNotCleanSchedules() {
        setEnableDelete(false);
        when(jobService.scheduleJob(any(), anyString())).thenReturn(new JobKey("name"));
        when(schedulerConfig.getSchedules()).thenReturn(asList(Schedule.builder().enabled(true).cron("cron").build()));

        classToTest.scheduleCronJobs();

        verify(jobService, times(1)).scheduleJob(any(), anyString());
        verify(jobService, never()).cleanSchedules(any());
    }

    @Test
    public void whenReCreateScheduleIsTrue_thenCleanSchedules() {
        setEnableDelete(true);
        when(jobService.scheduleJob(any(), anyString())).thenReturn(new JobKey("name"));
        when(schedulerConfig.getSchedules()).thenReturn(asList(
            Schedule.builder().enabled(true).cron("cron").build(),
            Schedule.builder().enabled(true).cron("cron").build()));

        classToTest.scheduleCronJobs();

        verify(jobService, times(2)).scheduleJob(any(), anyString());
        verify(jobService, times(1)).cleanSchedules(any());
    }

    @Test
    public void whenScheduleIsDisabled_thenDoNotProcess() {
        setEnableDelete(true);
        when(jobService.scheduleJob(any(), anyString())).thenReturn(new JobKey("name"));
        when(schedulerConfig.getSchedules()).thenReturn(asList(
            Schedule.builder().enabled(false).cron("cron").build(),
            Schedule.builder().enabled(true).cron("cron").build()));

        classToTest.scheduleCronJobs();

        verify(jobService, times(1)).scheduleJob(any(), anyString());
        verify(jobService, times(1)).cleanSchedules(any());
    }


    @Test
    public void givenException_whenCreateScheduleIsTrue_thenCleanSchedules() {
        setEnableDelete(true);

        doThrow(new JobException("Error message", new Exception())).when(jobService).cleanSchedules(any());
        when(schedulerConfig.getSchedules()).thenReturn(asList(Schedule.builder().enabled(true).cron("cron").build()));

        try {
            classToTest.scheduleCronJobs();
            fail("JobException expected");
        } catch (JobException e) {
            verify(jobService, never()).scheduleJob(any(), anyString());
            verify(jobService, times(1)).cleanSchedules(any());
        }
    }

    @Test(expected = JobException.class)
    public void givenException_whenCreateSchedule_thenPropagateError() {
        setEnableDelete(true);

        when(schedulerConfig.getSchedules()).thenReturn(asList(Schedule.builder().enabled(true).cron("cron").build()));
        when(jobService.scheduleJob(any(), anyString())).thenThrow(new JobException("Scheduler error", new Exception()));

        classToTest.scheduleCronJobs();
    }

    private void setEnableDelete(boolean value) {
        ReflectionTestUtils.setField(classToTest, "reCreateSchedules", value);
    }
}
