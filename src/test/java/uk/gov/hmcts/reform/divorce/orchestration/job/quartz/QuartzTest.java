package uk.gov.hmcts.reform.divorce.orchestration.job.quartz;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.quartz.impl.matchers.GroupMatcher.anyGroup;

/**
 * This abstract class should be extended by classes testing quartz jobs. It will use the scheduler's job configuration and trigger a job immediately.
 * This should give us early warnings in case there's some runtime errors with loading and executing Quartz jobs.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureEmbeddedDatabase
@TestPropertySource(properties = {
    "scheduler.enabled=true"
})
public abstract class QuartzTest {

    @Autowired
    private Scheduler scheduler;

    @Before
    public void setUp() {
        setUpQuartzTest();
    }

    protected abstract void setUpQuartzTest();

    @Test
    public void shouldAssertQuartzJobHasRun() throws SchedulerException {
        Class<? extends Job> jobUnderTest = getJobUnderTest();
        triggerJob(jobUnderTest);

        ThrowingRunnable assertion = getBasicAssertion();
        assertAsynchronously(jobUnderTest, assertion);
        assertThat(scheduler.getSchedulerName(), is("Divorce Job Scheduler"));
    }

    protected abstract Class<? extends Job> getJobUnderTest();

    protected abstract ThrowingRunnable getBasicAssertion();

    protected void triggerJob(Class<? extends Job> jobToTrigger) throws SchedulerException {
        JobKey jobToExecute = findJobInScheduler(jobToTrigger);

        if (jobToExecute != null) {
            scheduler.triggerJob(jobToExecute);
        } else {
            fail("Could not find " + jobToTrigger.getSimpleName()
                + " job in scheduler. Have you configured your test's environment variables to enable this test?");
        }
    }

    private JobKey findJobInScheduler(Class<? extends Job> jobToTrigger) throws SchedulerException {
        JobKey jobToExecute = null;

        for (JobKey jobKey : scheduler.getJobKeys(anyGroup())) {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            Class<? extends Job> jobClass = jobDetail.getJobClass();
            if (jobToTrigger.equals(jobClass)) {
                jobToExecute = jobKey;
                break;
            }
        }

        return jobToExecute;
    }

    protected void assertAsynchronously(Class<? extends Job> jobUnderTest, ThrowingRunnable assertion) {
        try {
            await().untilAsserted(assertion);
        } catch (ConditionTimeoutException exception) {
            String message = "The " + jobUnderTest.getSimpleName()
                + " job has failed to execute. Have a look into the errors before this one for more information on the reason.";

            Throwable cause = exception.getCause();
            if (cause instanceof MockitoAssertionError) {
                throw new MockitoAssertionError((MockitoAssertionError) cause, message);
            } else {
                throw new AssertionError(message, cause);
            }
        }
    }

}