package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.common;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
