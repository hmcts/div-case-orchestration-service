package uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception;

public class AuthenticationError extends Exception {
    public AuthenticationError(String message) {
        super(message);
    }
}
