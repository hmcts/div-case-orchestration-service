package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

/**
 * This service will be used by long running task or timed job trigger.
 */
public interface SchedulerService {

    /**
     * Schedule cron jobs.
     */
    void scheduleCronJobs() throws WorkflowException;

}
