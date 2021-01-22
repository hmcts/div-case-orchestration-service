package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Workflow<T> {

    private final ThreadLocal<DefaultTaskContext> threadLocalContext = new ThreadLocal<>();

    public T execute(Task<T>[] tasks, DefaultTaskContext context, T payload, Pair<String, Object>... pairs) throws WorkflowException {
        setContextVariables(context, pairs);

        try {
            for (Task<T> task : tasks) {
                if (getContext().hasTaskFailed()) {
                    break;
                }
                payload = task.execute(getContext(), payload);
            }
        } catch (TaskException e) {
            throw new WorkflowException(e.getMessage(), e);
        }

        return payload;
    }

    public T execute(Task<T>[] tasks, T payload, Pair<String, Object>... pairs) throws WorkflowException {
        return execute(tasks, new DefaultTaskContext(), payload, pairs);
    }

    private void setContextVariables(DefaultTaskContext context, Pair<String, Object>[] pairs) {
        threadLocalContext.set(context);
        for (Pair<String, Object> pair : pairs) {
            getContext().setTransientObject(pair.getKey(), pair.getValue());
        }
    }

    public DefaultTaskContext getContext() {
        return threadLocalContext.get();
    }

    public Map<String, Object> errors() {
        Set<Map.Entry<String, Object>> entrySet = threadLocalContext.get().getTransientObjects().entrySet();
        Map<String, Object> errors = new HashMap<>();

        for (Map.Entry<String, Object> entry : entrySet) {
            String key = entry.getKey();
            if (key.endsWith("_Error")) {
                errors.put(key, entry.getValue());
            }
        }

        return errors;
    }

}