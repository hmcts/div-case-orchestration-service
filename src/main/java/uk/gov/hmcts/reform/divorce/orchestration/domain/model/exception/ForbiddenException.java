package uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
