package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import lombok.Data;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
public class DefaultWorkflow<T> implements Workflow<T> {
    private DefaultTaskContext context = new DefaultTaskContext();

    @SuppressWarnings("unchecked")
    @Override
    public T execute(Task[] tasks, T payLoad, Object... params) throws WorkflowException {
        try {
            for (Task<T> task: tasks) {
                payLoad = task.execute(context, payLoad, params);
                if (context.getStatus()) { 
                    break;
                }
            }
        } catch (TaskException e) {
            throw new WorkflowException(e.getMessage());
        }

        return payLoad;
    }

    @Override
    public Map<String, Object> errors() {
        Set<Map.Entry<String, Object>> entrySet = context.getTransientObjects().entrySet();
        Map<String, Object> errors = new HashMap<>();

        for (Map.Entry entry: entrySet) {
            String key = (String) entry.getKey();
            if (key.endsWith("_Error")) {
                errors.put(key, entry.getValue());
            }
        }
        return errors;
    }
}
