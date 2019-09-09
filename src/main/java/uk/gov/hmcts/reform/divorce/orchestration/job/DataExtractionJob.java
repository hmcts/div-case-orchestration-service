package uk.gov.hmcts.reform.divorce.orchestration.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;

@Slf4j
public class DataExtractionJob implements Job {

    @Autowired
    private DataExtractionService dataExtractionService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            dataExtractionService.requestDataExtractionForPreviousDay();
            log.info("Data extraction requested");
        } catch (WorkflowException e) {
            throw new JobExecutionException("ExtractDataToRobotics service failed", e);
        }
    }

}