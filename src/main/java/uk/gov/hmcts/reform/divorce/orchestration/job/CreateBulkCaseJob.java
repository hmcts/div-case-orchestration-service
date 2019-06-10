package uk.gov.hmcts.reform.divorce.orchestration.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

@Slf4j
@RequiredArgsConstructor
public class CreateBulkCaseJob implements Job {

    private final CaseOrchestrationService orchestrationService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            orchestrationService.generateBulkCaseForListing();
            log.info("CreateBulkCaseJob executed");
        } catch (WorkflowException e) {
            throw new JobExecutionException("BulkCase creation failed", e);
        }
    }

}
