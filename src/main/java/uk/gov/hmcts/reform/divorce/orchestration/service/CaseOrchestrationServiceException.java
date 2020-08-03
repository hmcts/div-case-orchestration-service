package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Optional;

public class CaseOrchestrationServiceException extends Exception {

    private Optional<String> caseId = Optional.empty();

    public CaseOrchestrationServiceException(String message) {
        super(message);
    }

    public CaseOrchestrationServiceException(Exception exception) {
        super(exception.getMessage(), exception);
    }

    public CaseOrchestrationServiceException(WorkflowException exception, String caseId) {
        this(exception);
        this.caseId = Optional.ofNullable(caseId);
    }

    public Optional<String> getCaseId() {
        return caseId;
    }

    public String getIdentifiableMessage() {
        String exceptionMessage = getMessage();
        return caseId.map(value -> "Case id [" + value + "]: " + exceptionMessage).orElse(exceptionMessage);
    }

}