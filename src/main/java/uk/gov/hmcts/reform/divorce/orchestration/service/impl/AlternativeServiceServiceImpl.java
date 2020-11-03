package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AlternativeServiceService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.ConfirmAlternativeServiceWorkflow;

@Component
@AllArgsConstructor
public class AlternativeServiceServiceImpl implements AlternativeServiceService {

    private final ConfirmAlternativeServiceWorkflow confirmAlternativeServiceWorkflow;

    @Override
    public CaseDetails confirmAlternativeService(CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        try {
            return CaseDetails.builder().caseData(confirmAlternativeServiceWorkflow.run(caseDetails)).build();
        } catch (WorkflowException workflowException) {
            throw new CaseOrchestrationServiceException(workflowException, caseDetails.getCaseId());
        }
    }
}
