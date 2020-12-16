package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AlternativeServiceService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.AlternativeServiceConfirmedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.AosNotReceivedForProcessServerWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.ConfirmAlternativeServiceWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.ConfirmProcessServerServiceWorkflow;

import java.util.Map;

@Component
@AllArgsConstructor
public class AlternativeServiceServiceImpl implements AlternativeServiceService {

    private final ConfirmAlternativeServiceWorkflow confirmAlternativeServiceWorkflow;
    private final ConfirmProcessServerServiceWorkflow confirmProcessServerServiceWorkflow;
    private final AlternativeServiceConfirmedWorkflow alternativeServiceConfirmedWorkflow;
    private final AosNotReceivedForProcessServerWorkflow aosNotReceivedForProcessServerWorkflow;

    @Override
    public CaseDetails confirmAlternativeService(CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        try {
            return CaseDetails.builder().caseData(confirmAlternativeServiceWorkflow.run(caseDetails)).build();
        } catch (WorkflowException workflowException) {
            throw new CaseOrchestrationServiceException(workflowException, caseDetails.getCaseId());
        }
    }

    @Override
    public CaseDetails alternativeServiceConfirmed(CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        try {
            return CaseDetails.builder().caseData(alternativeServiceConfirmedWorkflow.run(caseDetails)).build();
        } catch (WorkflowException workflowException) {
            throw new CaseOrchestrationServiceException(workflowException, caseDetails.getCaseId());
        }
    }

    @Override
    public CaseDetails aosNotReceivedForProcessServer(CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        try {
            return CaseDetails.builder().caseData(aosNotReceivedForProcessServerWorkflow.run(caseDetails)).build();
        } catch (WorkflowException workflowException) {
            throw new CaseOrchestrationServiceException(workflowException, caseDetails.getCaseId());
        }
    }

    @Override
    public CaseDetails confirmProcessServerService(CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        try {
            Map<String, Object> caseData = confirmProcessServerServiceWorkflow.run(caseDetails);
            return CaseDetails.builder().caseData(caseData).build();
        } catch (WorkflowException workflowException) {
            throw new CaseOrchestrationServiceException(workflowException, caseDetails.getCaseId());
        }
    }
}
