package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralReferralService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral.GeneralConsiderationWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_GENERAL_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isGeneralReferralPaymentRequired;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralReferralServiceImpl implements GeneralReferralService {

    private final GeneralConsiderationWorkflow generalConsiderationWorkflow;

    @Override
    public CcdCallbackResponse receiveReferral(CcdCallbackRequest ccdCallbackRequest) {

        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        CcdCallbackResponse.CcdCallbackResponseBuilder responseBuilder = CcdCallbackResponse.builder();
        responseBuilder.data(caseData);

        if (isGeneralReferralPaymentRequired(caseData)) {
            responseBuilder.state(AWAITING_GENERAL_REFERRAL_PAYMENT);
            log.info("CaseID: {} Case state updated to {}", caseId, AWAITING_GENERAL_REFERRAL_PAYMENT);
        } else {
            responseBuilder.state(AWAITING_GENERAL_CONSIDERATION);
            log.info("CaseID: {} Case state updated to {}", caseId, AWAITING_GENERAL_CONSIDERATION);
        }

        CcdCallbackResponse ccdCallbackResponse = responseBuilder.build();
        log.info("CaseID: {} General Referral workflow complete. Case state is now {}", caseId, ccdCallbackResponse.getState());

        return ccdCallbackResponse;
    }

    @Override
    public Map<String, Object> generalConsideration(CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        try {
            return generalConsiderationWorkflow.run(caseDetails);
        } catch (WorkflowException workflowException) {
            throw new CaseOrchestrationServiceException(workflowException, caseDetails.getCaseId());
        }
    }
}
