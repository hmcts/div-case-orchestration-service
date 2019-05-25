package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobKey;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.CronJobSchedulerTask;
import uk.gov.hmcts.reform.divorce.orchestration.jobs.ScheduleTestJob;
import uk.gov.hmcts.reform.divorce.orchestration.models.Schedule;
import uk.gov.hmcts.reform.divorce.scheduler.exceptions.JobException;
import uk.gov.hmcts.reform.divorce.scheduler.model.JobData;
import uk.gov.hmcts.reform.divorce.scheduler.services.JobService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractJobSchedulerTaskUTest {

    @Mock
    private JobService jobService;

    private CronJobSchedulerTask classToTest;

    private Schedule schedule;

    @Before
    public void setup() {
        this.schedule = Schedule.builder().name("testSchedule").cron("909090")
                .jobClass(ScheduleTestJob.class).description("desc").build();
    }

    @Test
    public void testExecute() throws TaskException {
        classToTest = new CronJobSchedulerTask(jobService, schedule);
        when(jobService.scheduleJob(any(JobData.class), Mockito.eq(schedule.getCron()))).thenReturn(new JobKey("jobkey"));
        String actual = classToTest.execute(new DefaultTaskContext(), "payload");
        Assert.assertSame("jobkey", actual);
    }

    @Test(expected =  TaskException.class)
    public void testExecuteWithError() throws TaskException {
        classToTest = new CronJobSchedulerTask(jobService, schedule);
        when(jobService.scheduleJob(any(JobData.class), Mockito.eq(schedule.getCron()))).thenThrow(new JobException("Error", new RuntimeException()));
        String actual = classToTest.execute(new DefaultTaskContext(), "payload");
        Assert.assertSame("jobkey", actual);
    }

}