package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.SolicitorService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.IssuePersonalServicePackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.MigrateToPersonalServicePackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrievePbaNumbersWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.SendSolicitorPersonalServiceEmailWorkflow;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SolicitorServiceImpl implements SolicitorService {

    private final IssuePersonalServicePackWorkflow issuePersonalServicePackWorkflow;
    private final MigrateToPersonalServicePackWorkflow migrateToPersonalServicePackWorkflow;
    private final SendSolicitorPersonalServiceEmailWorkflow sendSolicitorPersonalServiceEmailWorkflow;
    private final RetrievePbaNumbersWorkflow retrievePbaNumbersWorkflow;

    @Override
    public Map<String, Object> issuePersonalServicePack(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {
        return issuePersonalServicePackWorkflow.run(callbackRequest, authToken);
    }

    @Override
    public Map<String, Object> migrateToPersonalServicePack(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {
        return migrateToPersonalServicePackWorkflow.run(callbackRequest, authToken);
    }

    @Override
    public Map<String, Object> sendSolicitorPersonalServiceEmail(CcdCallbackRequest callbackRequest) throws WorkflowException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        return sendSolicitorPersonalServiceEmailWorkflow.run(caseDetails.getCaseId(), caseDetails.getCaseData());
    }

    @Override
    public Map<String, Object> retrievePbaNumbers(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {
        return retrievePbaNumbersWorkflow.run(callbackRequest, authToken);
    }
}
