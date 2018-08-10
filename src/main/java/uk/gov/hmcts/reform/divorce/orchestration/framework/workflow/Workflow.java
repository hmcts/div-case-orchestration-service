package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;

import java.util.Map;

public interface Workflow<T> {

    T execute(Task[] tasks, T payLoad) throws WorkflowException;

    Map<String, Object> errors();
}
