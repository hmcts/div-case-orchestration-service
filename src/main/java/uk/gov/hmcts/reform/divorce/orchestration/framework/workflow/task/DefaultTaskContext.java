package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DefaultTaskContext implements TaskContext {

    private boolean status;

    private Map<String, Object> transientObjects = new HashMap<>();

    public DefaultTaskContext() {
        this.setTaskFailed(false);
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
