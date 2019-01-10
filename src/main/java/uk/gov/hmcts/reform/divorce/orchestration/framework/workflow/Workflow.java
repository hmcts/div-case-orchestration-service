package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;

import java.util.Map;

public interface Workflow<T> {

    T execute(Task[] tasks, T payload, Pair... pairs) throws WorkflowException;

    T execute(Task[] tasks, DefaultTaskContext context, T payload, Pair... pairs) throws WorkflowException;

    Map<String, Object> errors();
}
