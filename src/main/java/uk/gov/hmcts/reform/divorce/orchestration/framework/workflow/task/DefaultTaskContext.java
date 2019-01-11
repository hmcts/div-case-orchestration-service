package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DefaultTaskContext implements TaskContext {

    private boolean status;

    private Map<String, Object> transientObjects;

    public DefaultTaskContext() {
        this.status = false;
        transientObjects = new HashMap<>();
    }

    public DefaultTaskContext(DefaultTaskContext context) {
        this.status = context.getStatus();
        this.transientObjects = new HashMap<>(context.getTransientObjects());
    }


    @Override
    public void setTaskFailed(boolean status) {
        this.status = status;
    }

    @Override
    public boolean getStatus() {
        return status;
    }

    @Override
    public void setTransientObject(String key, Object jsonNode) {
        transientObjects.put(key, jsonNode);
    }

    @Override
    public Object getTransientObject(String key) {
        return transientObjects.get(key);
    }
}
