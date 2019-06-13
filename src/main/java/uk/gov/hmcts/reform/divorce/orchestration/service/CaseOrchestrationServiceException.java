package uk.gov.hmcts.reform.divorce.orchestration.service;

public class CaseOrchestrationServiceException extends Exception {

    public CaseOrchestrationServiceException(Exception exception) {
        super(exception.getMessage(), exception);
    }

}