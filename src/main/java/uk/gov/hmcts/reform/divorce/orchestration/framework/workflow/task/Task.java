package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

@FunctionalInterface
public interface Task<T> {

    T execute(TaskContext context, T payLoad, Object... params) throws TaskException;

}
