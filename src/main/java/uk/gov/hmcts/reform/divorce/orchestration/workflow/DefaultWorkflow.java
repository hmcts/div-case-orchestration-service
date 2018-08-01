package uk.gov.hmcts.reform.divorce.orchestration.workflow;

import uk.gov.hmcts.reform.divorce.orchestration.task.Payload;
import uk.gov.hmcts.reform.divorce.orchestration.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.task.TaskException;

public class DefaultWorkflow implements Workflow {

    @Override
    public Payload execute(Task[]  tasks, Payload initialPayload) throws WorkflowException {

        Payload output = initialPayload;

        try {
            for (Task task: tasks) {
                output =  task.execute(output);
            }
        } catch (TaskException e) {
            throw new WorkflowException(e.getMessage());
        }

        return output;
    }
}
