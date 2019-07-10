package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

public class DefaultWorkflow<T> extends AbstractWorkflow<T> {

    @SuppressWarnings("unchecked")
    @Override
    public T executeInternal(Task[] tasks, T payload) throws WorkflowException {
        try {
            for (Task<T> task: tasks) {
                if (getContext().hasTaskFailed()) {
                    break;
                }
                // todo the payload to be returned will be the one from the last task, hence we need to
                // create a map of task to task_payload
                payload = task.execute(getContext(), payload);
            }
        } catch (TaskException e) {
            throw new WorkflowException(e.getMessage(), e);
        }

        return payload;
    }
}
