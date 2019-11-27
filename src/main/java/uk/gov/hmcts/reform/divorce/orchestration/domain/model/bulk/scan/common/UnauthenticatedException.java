package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.common;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String message) {
        super(message);
    }
}
