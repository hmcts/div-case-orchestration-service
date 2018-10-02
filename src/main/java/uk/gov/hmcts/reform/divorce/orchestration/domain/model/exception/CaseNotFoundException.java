package uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception;

public class CaseNotFoundException extends Exception {
    public CaseNotFoundException(String message) {
        super(message);
    }
}
