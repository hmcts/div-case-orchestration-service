package uk.gov.hmcts.reform.divorce.orchestration.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

@Slf4j
public class AosOverdueJob implements Job {

    @Autowired
    private AosService aosService;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Running {} job", this.getClass().getSimpleName());

        try {
            aosService.findCasesForWhichAosIsOverdue(authUtil.getCaseworkerToken());
        } catch (CaseOrchestrationServiceException e) {
            JobExecutionException jobExecutionException = new JobExecutionException(e);
            log.error("Error when trying to run {}", this.getClass().getName(), jobExecutionException);
            throw jobExecutionException;
        }

    }

}