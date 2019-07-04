package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import java.util.Optional;

public interface TaskContext {

    void setTaskFailed(boolean status);

    boolean hasTaskFailed();

    void setTransientObject(String key, Object data);

    <T> T getTransientObject(String key);

    <T> Optional<T> getTransientObjectOptional(String key);

    <T> T computeTransientObjectIfAbsent(String key, T defaultVal);
}


