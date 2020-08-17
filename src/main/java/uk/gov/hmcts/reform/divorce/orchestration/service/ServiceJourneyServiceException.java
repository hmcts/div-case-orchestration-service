package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

public class ServiceJourneyServiceException extends CaseOrchestrationServiceException {
    public ServiceJourneyServiceException(String message) {
        super(message);
    }

    public ServiceJourneyServiceException(Exception exception) {
        super(exception);
    }

    public ServiceJourneyServiceException(WorkflowException exception, String caseId) {
        super(exception, caseId);
    }
}