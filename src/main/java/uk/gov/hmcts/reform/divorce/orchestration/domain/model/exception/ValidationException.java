package uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception;

public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
