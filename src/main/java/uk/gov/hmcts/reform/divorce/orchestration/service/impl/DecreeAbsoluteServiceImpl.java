package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.NotifyRespondentOfDARequestedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateDAOverdueWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateDNPronouncedCasesWorkflow;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecreeAbsoluteServiceImpl implements DecreeAbsoluteService {

    private final NotifyRespondentOfDARequestedWorkflow notifyRespondentOfDARequestedWorkflow;
    private final UpdateDNPronouncedCasesWorkflow updateDNPronouncedCasesWorkflow;
    private final UpdateDAOverdueWorkflow updateDAOverdueWorkflow;

    @Override
    public int enableCaseEligibleForDecreeAbsolute(String authToken) throws WorkflowException {
        log.info("Start processing cases eligible for DA ...");
        int casesProcessed = updateDNPronouncedCasesWorkflow.run(authToken);
        log.info(String.format("Completed processing cases [%d] eligible for DA", casesProcessed));
        return casesProcessed;
    }

    @Override
    public int processCaseOverdueForDecreeAbsolute(String authToken) throws WorkflowException {
        log.info("Start processing cases which are overdue for Decree Absolute...");
        int casesProcessed = updateDAOverdueWorkflow.run(authToken);
        log.info(String.format("Completed processing cases [%d] which are overdue for Decree Absolute", casesProcessed));
        return casesProcessed;
    }

    @Override
    public Map<String, Object> notifyRespondentOfDARequested(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        return notifyRespondentOfDARequestedWorkflow.run(ccdCallbackRequest);
    }

}
