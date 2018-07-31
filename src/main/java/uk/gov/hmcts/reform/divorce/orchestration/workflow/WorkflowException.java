package uk.gov.hmcts.reform.divorce.orchestration.workflow;

public class WorkflowException extends Exception {
    public WorkflowException(String message) {
        super(message);
    }
}
