package uk.gov.hmcts.reform.divorce.orchestration.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

@RequiredArgsConstructor
@Slf4j
public class AosOverdueJob implements Job {

    private final AosService aosService;
    private final AuthUtil authUtil;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            aosService.markCasesToBeMovedToAosOverdue(authUtil.getCaseworkerToken());
        } catch (CaseOrchestrationServiceException e) {
            JobExecutionException jobExecutionException = new JobExecutionException(e);
            log.error("Error when trying to run {}", this.getClass().getName(), jobExecutionException);
            throw jobExecutionException;
        }
    }

}