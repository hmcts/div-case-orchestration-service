package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateDNPronouncedCasesWorkflow;

@Slf4j
@Service
public class DecreeAbsoluteServiceImpl implements DecreeAbsoluteService {

    private UpdateDNPronouncedCasesWorkflow updateDNPronouncedCasesWorkflow;

    public DecreeAbsoluteServiceImpl(UpdateDNPronouncedCasesWorkflow updateDNPronouncedCasesWorkflow) {
        this.updateDNPronouncedCasesWorkflow = updateDNPronouncedCasesWorkflow;
    }

    @Override
    public int enableCaseEligibleForDecreeAbsolute(String authToken) throws WorkflowException {
        log.info("Start processing cases eligible for DA ...");
        int casesProcessed = updateDNPronouncedCasesWorkflow.run(authToken);
        log.info(String.format("Completed processing cases [%d] eligible for DA", casesProcessed));
        return casesProcessed;
    }

}