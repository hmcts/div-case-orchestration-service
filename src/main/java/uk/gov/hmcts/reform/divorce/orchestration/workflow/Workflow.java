package uk.gov.hmcts.reform.divorce.orchestration.workflow;

import uk.gov.hmcts.reform.divorce.orchestration.task.Payload;
import uk.gov.hmcts.reform.divorce.orchestration.task.Task;

public interface Workflow {

    Payload execute(Task[] tasks, Payload payLoad) throws WorkflowException;

}
