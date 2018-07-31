package uk.gov.hmcts.reform.divorce.orchestration.workflow;

public interface Workflow {

   Payload execute(Task[] tasks, Payload payLoad) throws WorkflowException;

}
