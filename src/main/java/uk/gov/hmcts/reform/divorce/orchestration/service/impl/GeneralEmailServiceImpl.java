package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalemail.ClearGeneralEmailFieldsWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalemail.GeneralEmailWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalemail.StoreGeneralEmailFieldsWorkflow;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeneralEmailServiceImpl implements GeneralEmailService {

    private final GeneralEmailWorkflow generalEmailWorkflow;
    private final ClearGeneralEmailFieldsWorkflow clearGeneralEmailFieldsWorkflow;
    private final StoreGeneralEmailFieldsWorkflow storeGeneralEmailFieldsWorkflow;

    @Override
    public Map<String, Object> clearGeneralEmailFields(CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        try {
            return clearGeneralEmailFieldsWorkflow.run(caseDetails);
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception, caseDetails.getCaseId());
        }
    }

    @Override
    public Map<String, Object> storeGeneralEmailFields(CaseDetails caseDetails, String authorizationToken) throws CaseOrchestrationServiceException {
        try {
            return storeGeneralEmailFieldsWorkflow.run(caseDetails, authorizationToken);
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception, caseDetails.getCaseId());
        }
    }

    @Override
    public Map<String, Object> createGeneralEmail(CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        try {
            return generalEmailWorkflow.run(caseDetails);
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception, caseDetails.getCaseId());
        }
    }
}
