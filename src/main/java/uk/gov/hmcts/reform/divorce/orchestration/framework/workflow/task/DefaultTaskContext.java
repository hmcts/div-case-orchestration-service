package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DefaultTaskContext implements TaskContext {

    private boolean taskFailed;

    private Map<String, Object> transientObjects;

    public DefaultTaskContext() {
        this.taskFailed = false;
        transientObjects = new HashMap<>();
    }

    public DefaultTaskContext(DefaultTaskContext context) {
        this.taskFailed = context.hasTaskFailed();
        this.transientObjects = new HashMap<>(context.getTransientObjects());
    }

    @Override
    public void setTaskFailed(boolean status) {
        this.taskFailed = status;
    }

    @Override
    public boolean hasTaskFailed() {
        return taskFailed;
    }

    @Override
    public void setTransientObject(String key, Object value) {
        transientObjects.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getTransientObject(String key) {
        return (T) transientObjects.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T computeTransientObjectIfAbsent(final String key, final T defaultVal) {
        return (T) transientObjects.computeIfAbsent(key, k -> defaultVal);
    }
}
