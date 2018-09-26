package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;

@Component
public class SetCaseIdAndStateOnSession implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        caseData.put(CASE_ID_JSON_KEY, context.getTransientObject(CASE_ID_JSON_KEY));
        caseData.put(CASE_STATE_JSON_KEY, context.getTransientObject(CASE_STATE_JSON_KEY));

        return caseData;
    }
}