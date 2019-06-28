package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.event.CleanStatusEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseEventService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CleanStateFromCaseDataWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Service
@RequiredArgsConstructor
public class CaseEventServiceImpl implements CaseEventService {
    private final CleanStateFromCaseDataWorkflow cleanStateFromCaseDataWorkflow;

    @Override
    @EventListener
    public Map<String, Object> cleanStateFromData(CleanStatusEvent event) throws WorkflowException {
        DefaultTaskContext context = (DefaultTaskContext) event.getSource();
        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        return cleanStateFromCaseDataWorkflow.run(caseId, authToken);
    }
}
