package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public abstract class RemovalFieldTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        String field = getFieldToRemove();
        String caseId = getCaseId(context);
        log.info("CaseID: {} Removing field {} from caseData.", caseId, field);

        if (!payload.containsKey(field)) {
            log.warn("CaseID: {} Field {} doesn't exist in case data!", caseId, field);
        }

        payload.remove(field);

        return payload;
    }

    protected abstract String getFieldToRemove();
}
