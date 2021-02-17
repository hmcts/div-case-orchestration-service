package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralReferralService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.FurtherPaymentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral.GeneralConsiderationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral.GeneralReferralWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral.SetupGeneralReferralPaymentWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_GENERAL_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.FurtherPaymentsHelper.getGeneralReferralPaymentType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isGeneralReferralPaymentRequired;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralReferralServiceImpl implements GeneralReferralService {

    private final GeneralReferralWorkflow generalReferralWorkflow;
    private final GeneralConsiderationWorkflow generalConsiderationWorkflow;
    private final SetupGeneralReferralPaymentWorkflow setupGeneralReferralPaymentWorkflow;
    private final FurtherPaymentWorkflow furtherPaymentWorkflow;

    @Override
    public CcdCallbackResponse receiveReferral(CcdCallbackRequest ccdCallbackRequest)
        throws CaseOrchestrationServiceException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        String caseId = caseDetails.getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder responseBuilder = CcdCallbackResponse.builder();

        String state = isGeneralReferralPaymentRequired(caseDetails.getCaseData())
            ? AWAITING_GENERAL_REFERRAL_PAYMENT
            : AWAITING_GENERAL_CONSIDERATION;

        try {
            responseBuilder.data(generalReferralWorkflow.run(caseDetails));
        } catch (WorkflowException workflowException) {
            throw new CaseOrchestrationServiceException(workflowException, caseId);
        }

        log.info("CaseID: {} Case state updated to {}", caseId, state);

        return responseBuilder.state(state).build();
    }

    @Override
    public Map<String, Object> generalConsideration(CaseDetails caseDetails)
        throws CaseOrchestrationServiceException {
        try {
            return generalConsiderationWorkflow.run(caseDetails);
        } catch (WorkflowException workflowException) {
            throw new CaseOrchestrationServiceException(workflowException, caseDetails.getCaseId());
        }
    }

    @Override
    public Map<String, Object> setupGeneralReferralPaymentEvent(CaseDetails caseDetails)
        throws CaseOrchestrationServiceException {
        try {
            return setupGeneralReferralPaymentWorkflow.run(caseDetails);
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception, caseDetails.getCaseId());
        }
    }

    @Override
    public CcdCallbackResponse returnToStateBeforeGeneralReferral(CaseDetails caseDetails)
        throws CaseOrchestrationServiceException {
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        try {
            String previousCaseState = getMandatoryPropertyValueAsString(caseData, CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE);

            log.info("CaseID: {} Case state updated to {}", caseId, previousCaseState);

            return CcdCallbackResponse.builder()
                .state(previousCaseState)
                .data(caseData)
                .build();
        } catch (TaskException taskException) {
            throw new CaseOrchestrationServiceException(
                new WorkflowException(taskException.getMessage(), taskException), caseId);
        }
    }

    @Override
    public Map<String, Object> generalReferralPaymentEvent(CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        try {
            return furtherPaymentWorkflow.run(caseDetails, getGeneralReferralPaymentType());
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception, caseDetails.getCaseId());
        }
    }
}
