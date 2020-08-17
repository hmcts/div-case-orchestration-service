package uk.gov.hmcts.reform.divorce.orchestration.service;

public class ServiceJourneyServiceException extends CaseOrchestrationServiceException {
    public ServiceJourneyServiceException(Exception exception) {
        super(exception);
    }
}