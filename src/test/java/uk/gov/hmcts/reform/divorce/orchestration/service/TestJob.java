package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Test Job
 */
public class TestJob implements Job {

    public TestJob() {
        //EMPTY
    }
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        for (int i =0 ; i< 25; i++)
            System.out.println("\n\n *************************** test job ************* \n\n");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
           throw new JobExecutionException(e.getCause());
        }
    }
 }
