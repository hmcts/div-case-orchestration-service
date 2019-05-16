package uk.gov.hmcts.reform.divorce.orchestration.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

/**
 * Due date job
 */
public class DueDateJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //step 1 - Get all the cases
        try {
            System.out.println(" ..... \n'n DUE DATE " + jobExecutionContext.getScheduler().getSchedulerName() +"\n\n");
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        //step 2 - Iterate through cases and toggle.
    }
}
