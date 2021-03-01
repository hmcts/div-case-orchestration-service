package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.BailiffPackService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.bailiff.IssueBailiffPackWorkflow;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BailiffPackServiceImpl implements BailiffPackService {

    private final IssueBailiffPackWorkflow issueBailiffPackWorkflow;

    @Override
    public Map<String, Object> issueCertificateOfServiceDocument(String authorizationToken, CaseDetails caseDetails)
        throws CaseOrchestrationServiceException {

        String caseId = caseDetails.getCaseId();
        try {
            return issueBailiffPackWorkflow.issueCertificateOfServiceDocument(authorizationToken, caseId, caseDetails.getCaseData());
        } catch (WorkflowException workflowException) {
            throw new CaseOrchestrationServiceException(workflowException, caseId);
        }
    }
}
