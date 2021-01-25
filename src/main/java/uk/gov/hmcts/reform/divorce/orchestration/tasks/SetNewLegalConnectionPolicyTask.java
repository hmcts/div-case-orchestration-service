package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_LEGAL_CONNECTION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SetNewLegalConnectionPolicyTask implements Task<Map<String, Object>> {

    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        caseData.put(NEW_LEGAL_CONNECTION_POLICY, YES_VALUE);

        return caseData;
    }
}
