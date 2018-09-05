package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultWorkflow<T> implements Workflow<T> {
    private DefaultTaskContext context = new DefaultTaskContext();

    @Override
    public T execute(Task[] tasks, T payload, Pair... pairs) throws WorkflowException {
        for (Pair pair : pairs) {
            context.setTransientObject(pair.getKey().toString(), pair.getValue());
        }

        try {
            for (Task<T> task: tasks) {
                payload = task.execute(context, payload);
                if (context.getStatus()) {
                    break;
                }
            }
        } catch (TaskException e) {
            throw new WorkflowException(e.getMessage());
        }

        return payload;
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
