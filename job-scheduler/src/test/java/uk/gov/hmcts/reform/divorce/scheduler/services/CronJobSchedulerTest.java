package uk.gov.hmcts.reform.divorce.scheduler.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.scheduler.config.SchedulerConfig;
import uk.gov.hmcts.reform.divorce.scheduler.model.Schedule;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    public void whenDeleteOldScheduleIsFalse_thenDoNotCleanSchedules() throws SchedulerException {
        setEnableDelete(false);
        when(jobService.scheduleJob(any(), anyString())).thenReturn(new JobKey("name"));
        when(schedulerConfig.getSchedules()).thenReturn(Arrays.asList(Schedule.builder().cron("cron").build()));

        classToTest.scheduleCronJobs();

        verify(jobService, times(1)).scheduleJob(any(), anyString());
        verify(jobService, never()).cleanSchedules(any());
    }

    @Test
    public void whenDeleteOldScheduleIsTrue_thenCleanSchedules() throws SchedulerException {
        setEnableDelete(true);
        when(jobService.scheduleJob(any(), anyString())).thenReturn(new JobKey("name"));
        when(schedulerConfig.getSchedules()).thenReturn(Arrays.asList(Schedule.builder().cron("cron").build()));

        classToTest.scheduleCronJobs();

        verify(jobService, times(1)).scheduleJob(any(), anyString());
        verify(jobService, times(1)).cleanSchedules(any());
    }

    private void setEnableDelete(boolean value) {
        ReflectionTestUtils.setField(classToTest, "deleteOldSchedules", value);
    }
}
