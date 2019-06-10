package uk.gov.hmcts.reform.divorce.scheduler.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import uk.gov.hmcts.reform.divorce.scheduler.exception.JobException;
import uk.gov.hmcts.reform.divorce.scheduler.model.JobData;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest {
    private static final String CRON_EXPRESSION = "* * * * * ? *";
    private static final String GROUP = "Test group";
    private static final String JOB_ID = UUID.randomUUID().toString();

    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private JobService classToTest;

    @Test(expected = JobException.class)
    public void whenScheduleWithTimezoneFails_raiseJobException() throws SchedulerException {
        JobData jobData = getJobData(JOB_ID, GROUP);
        ZonedDateTime startDateTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC);

        when(scheduler.scheduleJob(any(), any())).thenThrow(new SchedulerException());

        classToTest.scheduleJob(jobData, startDateTime);
    }

    @Test(expected = JobException.class)
    public void whenScheduleFails_raiseJobException() throws SchedulerException {
        JobData jobData = getJobData(JOB_ID, GROUP);

        when(scheduler.scheduleJob(any(), any())).thenThrow(new SchedulerException());

        classToTest.scheduleJob(jobData, CRON_EXPRESSION);
    }

    @Test
    public void givenCron_whenScheduleJob_shouldScheduleNewJob() throws SchedulerException {
        //given
        JobData jobData = getJobData(JOB_ID, GROUP);
        ZonedDateTime startDateTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC);

        //when
        JobKey jobKey = classToTest.scheduleJob(jobData, CRON_EXPRESSION);

        //then
        assertThat(jobKey).isEqualTo(new JobKey(JOB_ID, GROUP));
        JobDetail jobDetail = getJobDetails(jobData);
        Trigger trigger = getTrigger(startDateTime, jobData);
        verify(scheduler).scheduleJob(jobDetail, trigger);
    }

    @Test
    public void shouldScheduleNewJob() throws SchedulerException {
        //given
        JobData jobData = getJobData(JOB_ID, GROUP);
        ZonedDateTime startDateTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC);

        //when
        JobKey jobKey = classToTest.scheduleJob(jobData, startDateTime);

        //then
        assertThat(jobKey).isEqualTo(new JobKey(JOB_ID, GROUP));
        JobDetail jobDetail = getJobDetails(jobData);
        Trigger trigger = getTrigger(startDateTime, jobData);
        verify(scheduler).scheduleJob(jobDetail, trigger);
    }

    @Test
    public void shouldRescheduleOldJob() throws SchedulerException {
        //given
        JobData jobData = getJobData(JOB_ID, GROUP);
        ZonedDateTime startDateTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC);

        //when

        //schedule new job
        JobKey jobKey = classToTest.scheduleJob(jobData, startDateTime);

        //reschedule job again
        ZonedDateTime newStartDateTime = LocalDate.now().plusDays(5).atStartOfDay(ZoneOffset.UTC);
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class)))
            .thenReturn(Date.from(newStartDateTime.toInstant()));

        classToTest.rescheduleJob(jobData, newStartDateTime);

        //then

        //verify scheduling
        assertThat(jobKey).isEqualTo(new JobKey(JOB_ID, GROUP));
        verify(scheduler).scheduleJob(getJobDetails(jobData), getTrigger(startDateTime, jobData));

        //verify rescheduling
        verify(scheduler).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }

    @Test(expected = JobException.class)
    public void givenError_whenRescheduleJob_thenPropagateError() throws SchedulerException {
        JobData jobData = getJobData(JOB_ID, GROUP);
        ZonedDateTime startTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC);

        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class))).thenThrow(new SchedulerException());

        classToTest.rescheduleJob(jobData, startTime);
    }

    @Test
    public void shouldScheduleAnotherJobWhenReschedulingOldJobFails() throws SchedulerException {
        //given
        JobData jobData = getJobData(JOB_ID, GROUP);

        ZonedDateTime startDateTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC);

        //when
        //schedule new job
        JobKey jobKey = classToTest.scheduleJob(jobData, startDateTime);

        //reschedule job again
        ZonedDateTime newStartDateTime = LocalDate.now().plusDays(5).atStartOfDay(ZoneOffset.UTC);
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class)))
            .thenReturn(null);

        classToTest.rescheduleJob(jobData, newStartDateTime);

        //then
        //verify scheduling
        assertThat(jobKey).isEqualTo(new JobKey(JOB_ID, GROUP));
        verify(scheduler, atLeast(2)).scheduleJob(getJobDetails(jobData), getTrigger(startDateTime, jobData));
        //verify rescheduling
        verify(scheduler).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }

    @Test
    public void shouldCleanAllSchedules() throws SchedulerException {
        String schedule1 = "schedule1";
        String schedule2 = "schedule1";
        JobKey jobKey = new JobKey(schedule1);
        JobKey jobKey2 = new JobKey(schedule2);

        when(scheduler.getJobKeys(GroupMatcher.groupEquals(schedule1))).thenReturn(Sets.newSet(jobKey));
        when(scheduler.getJobKeys(GroupMatcher.groupEquals(schedule2))).thenReturn(Sets.newSet(jobKey2));

        classToTest.cleanSchedules(schedule1, schedule2);

        verify(scheduler, times(2)).deleteJob(any());
    }

    @Test(expected = JobException.class)
    public void givenError_whenCleanSchedule_thenPropagateException() throws SchedulerException {
        String schedule1 = "schedule1";
        String schedule2 = "schedule1";
        JobKey jobKey = new JobKey(schedule1);
        JobKey jobKey2 = new JobKey(schedule2);

        when(scheduler.getJobKeys(GroupMatcher.groupEquals(schedule1))).thenReturn(Sets.newSet(jobKey));
        when(scheduler.getJobKeys(GroupMatcher.groupEquals(schedule2))).thenReturn(Sets.newSet(jobKey2));
        when(scheduler.deleteJob(jobKey)).thenThrow(new SchedulerException());

        classToTest.cleanSchedules(schedule1, schedule2);
    }

    private JobData getJobData(String jobId, String group) {
        Map<String, Object> data = new HashMap<>();
        data.put("caseId", "234324332432432");
        data.put("caseReference", "000MC003");
        data.put("defendantEmail", "j.smith@example.com");

        return JobData.builder()
            .id(jobId)
            .group(group)
            .description("Mock job scheduler")
            .data(data)
            .jobClass(Job.class)
            .build();
    }

    private Trigger getTrigger(ZonedDateTime startDateTime, JobData jobData) {
        return newTrigger()
            .startAt(Date.from(startDateTime.toInstant()))
            .withIdentity(jobData.getId(), jobData.getGroup())
            .withSchedule(
                simpleSchedule()
                    .withMisfireHandlingInstructionNowWithExistingCount()
            )
            .build();
    }

    private JobDetail getJobDetails(JobData jobData) {
        return JobBuilder.newJob(jobData.getJobClass())
            .withIdentity(jobData.getId(), jobData.getGroup())
            .withDescription(jobData.getDescription())
            .usingJobData(new JobDataMap(jobData.getData()))
            .requestRecovery()
            .build();
    }

}


