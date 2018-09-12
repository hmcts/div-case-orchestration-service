package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

abstract class AbstractWorkflow<T> implements Workflow<T> {
    private final ThreadLocal<DefaultTaskContext> threadLocalContext = new ThreadLocal<>();

    @Override
    public T execute(Task[] tasks, T payload, Pair... pairs) throws WorkflowException {
        threadLocalContext.set(new DefaultTaskContext());

        for (Pair pair : pairs) {
            getContext().setTransientObject(pair.getKey().toString(), pair.getValue());
        }

        return executeInternal(tasks, payload);
    }

    public DefaultTaskContext getContext() {
        return threadLocalContext.get();
    }

    @Override
    public Map<String, Object> errors() {
        Set<Map.Entry<String, Object>> entrySet = threadLocalContext.get().getTransientObjects().entrySet();
        Map<String, Object> errors = new HashMap<>();

        for (Map.Entry entry: entrySet) {
            String key = (String) entry.getKey();
            if (key.endsWith("_Error")) {
                errors.put(key, entry.getValue());
            }
        }

        return errors;
    }

    protected abstract T executeInternal(Task[] tasks, T payload) throws WorkflowException;
}
