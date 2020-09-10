package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

/**
 * Implement <pre>List<String> getFieldsToRemove()</pre> in your class that extends this one
 * and add it to your workflow to remove these fields from caseData.
 * Mind if you update case data in "submitted" callback it will not be persisted in CCD.
 */
@Component
@Slf4j
public abstract class FieldsRemovalTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        List<String> fieldsToRemove = getFieldsToRemove();
        String caseId = getCaseId(context);

        log.info("CaseID: {} Removing {} fields from caseData.", caseId, fieldsToRemove.size());

        fieldsToRemove.forEach(field -> {
            log.info("CaseID: {} Removing field '{}' from caseData.", caseId, field);

            if (!payload.containsKey(field)) {
                log.warn("CaseID: {} Field '{}' doesn't exist in case data!", caseId, field);
            }

            payload.remove(field);
        });

        return payload;
    }

    protected abstract List<String> getFieldsToRemove();
}
