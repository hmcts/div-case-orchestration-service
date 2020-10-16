package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralReferralService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral.GeneralReferralWorkflow;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_GENERAL_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isGeneralReferralPaymentRequired;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralReferralServiceImpl implements GeneralReferralService {

    private final GeneralReferralWorkflow generalReferralWorkflow;

    @Override
    public CcdCallbackResponse receiveReferral(CcdCallbackRequest ccdCallbackRequest, String authorizationToken)
        throws CaseOrchestrationServiceException {

        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        String caseId = caseDetails.getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder responseBuilder = CcdCallbackResponse.builder();
        try {
            responseBuilder.data(generalReferralWorkflow.run(caseDetails, authorizationToken));

            if (isGeneralReferralPaymentRequired(caseDetails.getCaseData())) {
                responseBuilder.state(AWAITING_GENERAL_REFERRAL_PAYMENT);
                log.info("CaseID: {} Case state updated to {}", caseId, CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT);
            } else {
                responseBuilder.state(AWAITING_GENERAL_CONSIDERATION);
                log.info("CaseID: {} Case state updated to {}", caseId, CcdStates.AWAITING_GENERAL_CONSIDERATION);
            }
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception, caseId);
        }

        CcdCallbackResponse ccdCallbackResponse = responseBuilder.build();
        log.info("CaseID: {} General Referral workflow complete. Case state is now {}", caseId, ccdCallbackResponse.getState());

        return ccdCallbackResponse;
    }
}
