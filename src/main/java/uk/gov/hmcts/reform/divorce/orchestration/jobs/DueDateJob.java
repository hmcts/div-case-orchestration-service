package uk.gov.hmcts.reform.divorce.orchestration.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

@Slf4j
public class DueDateJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //step 1 - Get all the cases
        try {
            log.info(" ..... \n'n DUE DATE " + jobExecutionContext.getScheduler().getSchedulerName() + "\n\n");
        } catch (SchedulerException e) {
            log.error("Error on scheduler", e);
        }
        //step 2 - Iterate through cases and toggle.
    }
}
