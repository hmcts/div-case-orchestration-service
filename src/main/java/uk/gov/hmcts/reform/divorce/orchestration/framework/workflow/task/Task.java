package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.Template;

@FunctionalInterface
public interface Task<T> extends Template {
    T execute(TaskContext context, T payload) throws TaskException;
}
    