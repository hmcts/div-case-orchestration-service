package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateDNPronouncedCasesWorkflow;

@Slf4j
@Service
public class DecreeAbsoluteServiceImpl implements DecreeAbsoluteService {

    private final UpdateDNPronouncedCasesWorkflow updateDNPronouncedCasesWorkflow;
    private final AuthUtil authUtil;

    @Autowired
    public DecreeAbsoluteServiceImpl(UpdateDNPronouncedCasesWorkflow updateDNPronouncedCasesWorkflow,
                                     AuthUtil authUtil) {
        this.updateDNPronouncedCasesWorkflow = updateDNPronouncedCasesWorkflow;
        this.authUtil = authUtil;
    }

    @Override
    public int enableCaseEligibleForDecreeAbsolute() throws WorkflowException {
        log.info("Start processing cases eligible for DA ...");
        int casesProcessed = updateDNPronouncedCasesWorkflow.run(authUtil.getCaseworkerToken());
        log.info(String.format("Completed processing cases [%d] eligible for DA", casesProcessed));
        return casesProcessed;
    }
}
