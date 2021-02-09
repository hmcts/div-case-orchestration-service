package uk.gov.hmcts.reform.divorce.orchestration.exception;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;

public class GeneralOrderServiceException extends CaseOrchestrationServiceException {
    public GeneralOrderServiceException(WorkflowException exception, String caseId) {
        super(exception, caseId);
    }
}
