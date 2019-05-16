package uk.gov.hmcts.reform.divorce.scheduler.exceptions;

public class JobException extends RuntimeException {

    public JobException(String message, Throwable cause) {
        super(message, cause);
    }
}
