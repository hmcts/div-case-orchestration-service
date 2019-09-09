package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_OUTCOME_FLAG_CCD_FIELD;

@Component
public class RemoveDnOutcomeCaseFlagTask implements Task<Map<String, Object>> {
    @Override
    public Map<String, Object> execute(TaskContext context, final Map<String, Object> payload) throws TaskException {
        Map<String, Object> response = new HashMap<>(payload);
        response.remove(DN_OUTCOME_FLAG_CCD_FIELD);
        return response;
    }
}
