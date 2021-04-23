package uk.gov.hmcts.reform.divorce.orchestration.exception;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Optional;

public class JudgeServiceException extends Exception {

    private String caseId;

    public JudgeServiceException(String message) {
        super(message);
    }

    public JudgeServiceException(Exception exception) {
        super(exception.getMessage(), exception);
    }

    public JudgeServiceException(WorkflowException exception, String caseId) {
        this(exception);
        this.caseId = caseId;
    }

    public Optional<String> getCaseId() {
        return Optional.ofNullable(caseId);
    }

    public String getIdentifiableMessage() {
        String exceptionMessage = getMessage();
        return getCaseId().map(value -> "Case id [" + value + "]: " + exceptionMessage).orElse(exceptionMessage);
    }

}
