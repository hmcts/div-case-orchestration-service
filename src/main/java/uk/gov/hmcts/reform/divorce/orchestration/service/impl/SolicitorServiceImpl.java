package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.SolicitorService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrievePbaNumbersWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolConfirmServiceWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ValidateForPersonalServicePackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.SendSolicitorPersonalServiceEmailWorkflow;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SolicitorServiceImpl implements SolicitorService {

    private final SolConfirmServiceWorkflow solConfirmServiceWorkflow;
    private final ValidateForPersonalServicePackWorkflow validateForPersonalServicePackWorkflow;
    private final SendSolicitorPersonalServiceEmailWorkflow sendSolicitorPersonalServiceEmailWorkflow;
    private final RetrievePbaNumbersWorkflow retrievePbaNumbersWorkflow;

    @Override
    public Map<String, Object> validateForPersonalServicePack(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {
        return validateForPersonalServicePackWorkflow.run(callbackRequest, authToken);
    }

    @Override
    public Map<String, Object> solicitorConfirmPersonalService(CcdCallbackRequest callbackRequest) throws WorkflowException {
        return solConfirmServiceWorkflow.run(callbackRequest);
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
