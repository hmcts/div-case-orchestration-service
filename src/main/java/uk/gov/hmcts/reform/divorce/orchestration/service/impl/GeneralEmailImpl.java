package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.exception.GeneralOrderServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GeneralEmailWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder.GenerateGeneralOrderDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder.GenerateGeneralOrderWorkflow;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeneralEmailImpl implements GeneralEmailService {

    private final GeneralEmailWorkflow generalEmailWorkflow;

    @Override
    public Map<String, Object> createGeneralEmail(CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        try {
            return generalEmailWorkflow.run(caseDetails);
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception, caseDetails.getCaseId());
        }
    }
}
