package uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String message) {
        super(message);
    }

}
