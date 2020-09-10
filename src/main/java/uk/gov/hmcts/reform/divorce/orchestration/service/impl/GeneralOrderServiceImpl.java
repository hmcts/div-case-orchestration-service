package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.exception.GeneralOrderServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder.GenerateGeneralOrderDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder.GenerateGeneralOrderWorkflow;

@Component
@RequiredArgsConstructor
public class GeneralOrderServiceImpl implements GeneralOrderService {

    private final GenerateGeneralOrderWorkflow generateGeneralOrderWorkflow;
    private final GenerateGeneralOrderDraftWorkflow generateGeneralOrderDraftWorkflow;

    @Override
    public CaseDetails generateGeneralOrder(CaseDetails caseDetails, String authorisation)
        throws GeneralOrderServiceException {
        try {
            return CaseDetails.builder()
                .caseData(generateGeneralOrderWorkflow.run(caseDetails, authorisation))
                .build();
        } catch (WorkflowException workflowException) {
            throw new GeneralOrderServiceException(workflowException);
        }
    }

    @Override
    public CaseDetails generateGeneralOrderDraft(CaseDetails caseDetails, String authorisation)
        throws GeneralOrderServiceException {
        try {
            return CaseDetails.builder()
                .caseData(generateGeneralOrderDraftWorkflow.run(caseDetails, authorisation))
                .build();
        } catch (WorkflowException workflowException) {
            throw new GeneralOrderServiceException(workflowException);
        }
    }
}
