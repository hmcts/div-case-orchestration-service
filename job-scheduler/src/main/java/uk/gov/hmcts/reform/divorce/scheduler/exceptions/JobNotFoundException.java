package uk.gov.hmcts.reform.divorce.scheduler.exceptions;

public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(String message) {
        super(message);
    }
}
