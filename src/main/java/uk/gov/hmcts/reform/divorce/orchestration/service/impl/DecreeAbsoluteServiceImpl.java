package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.NotifyRespondentOfDARequestedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateDNPronouncedCasesWorkflow;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecreeAbsoluteServiceImpl implements DecreeAbsoluteService {

    private final NotifyRespondentOfDARequestedWorkflow notifyRespondentOfDARequestedWorkflow;
    private final UpdateDNPronouncedCasesWorkflow updateDNPronouncedCasesWorkflow;

    @Override
    public int enableCaseEligibleForDecreeAbsolute(String authToken) throws WorkflowException {
        log.info("Start processing cases eligible for DA ...");
        int casesProcessed = updateDNPronouncedCasesWorkflow.run(authToken);
        log.info(String.format("Completed processing cases [%d] eligible for DA", casesProcessed));
        return casesProcessed;
    }

    @Override
    public Map<String, Object> notifyRespondentOfDARequested(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        return notifyRespondentOfDARequestedWorkflow.run(ccdCallbackRequest);
    }

}
