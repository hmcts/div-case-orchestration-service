package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

public interface TaskContext {

    void setTaskFailed(boolean status);

   boolean getStatus();

   void setTransientObject(String key, Object jsonNode);

   Object getTransientObject(String key);

}


