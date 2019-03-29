package uk.gov.hmcts.reform.divorce.orchestration.service;

public class CaseOrchestrarionServiceException extends Exception {

    public CaseOrchestrarionServiceException(Exception exception) {
        super(exception.getMessage(), exception);
    }

}