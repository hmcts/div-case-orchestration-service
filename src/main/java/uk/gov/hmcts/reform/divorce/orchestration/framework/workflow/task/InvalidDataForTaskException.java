package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

/*
 * Use it in Task when data is not found / invalid.
 */
public class InvalidDataForTaskException extends TaskException {

    public InvalidDataForTaskException(Throwable cause) {
        super(cause);
    }

    public InvalidDataForTaskException(String message) {
        super(message);
    }

}