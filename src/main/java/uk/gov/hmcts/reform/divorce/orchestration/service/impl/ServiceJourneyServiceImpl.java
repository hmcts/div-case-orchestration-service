package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyService;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.FurtherPaymentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ReceivedServiceAddedDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceDecisionMadeWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceDecisionMakingWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.SetupConfirmServicePaymentWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_BAILIFF_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_BAILIFF_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationBailiff;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;
import static uk.gov.hmcts.reform.divorce.orchestration.util.FurtherPaymentsHelper.getServiceApplicationPaymentType;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceJourneyServiceImpl implements ServiceJourneyService {

    private final ReceivedServiceAddedDateWorkflow receivedServiceAddedDateWorkflow;
    private final MakeServiceDecisionWorkflow makeServiceDecisionWorkflow;
    private final ServiceDecisionMadeWorkflow serviceDecisionMadeWorkflow;
    private final ServiceDecisionMakingWorkflow serviceDecisionMakingWorkflow;
    private final SetupConfirmServicePaymentWorkflow setupConfirmServicePaymentWorkflow;
    private final FurtherPaymentWorkflow furtherPaymentWorkflow;

    @Override
    public CcdCallbackResponse makeServiceDecision(CaseDetails caseDetails, String authorisation) throws ServiceJourneyServiceException {
        CcdCallbackResponse.CcdCallbackResponseBuilder builder = CcdCallbackResponse.builder();

        if (isServiceApplicationGranted(caseDetails.getCaseData())) {
            if (isServiceApplicationBailiff(caseDetails.getCaseData())) {
                builder.state(AWAITING_BAILIFF_SERVICE);
            } else {
                builder.state(AWAITING_DECREE_NISI);
            }
        } else {
            builder.state(SERVICE_APPLICATION_NOT_APPROVED);
        }

        try {
            builder.data(makeServiceDecisionWorkflow.run(caseDetails, authorisation));
        } catch (WorkflowException workflowException) {
            throw new ServiceJourneyServiceException(workflowException);
        }

        return builder.build();
    }

    @Override
    public Map<String, Object> receivedServiceAddedDate(CcdCallbackRequest ccdCallbackRequest)
        throws ServiceJourneyServiceException {
        try {
            return receivedServiceAddedDateWorkflow.run(ccdCallbackRequest.getCaseDetails());
        } catch (WorkflowException workflowException) {
            throw new ServiceJourneyServiceException(workflowException);
        }
    }

    @Override
    public CcdCallbackResponse serviceDecisionMade(CaseDetails caseDetails, String authorisation) throws ServiceJourneyServiceException {
        try {
            return CcdCallbackResponse.builder()
                .data(serviceDecisionMadeWorkflow.run(caseDetails, authorisation))
                .build();
        } catch (WorkflowException e) {
            throw new ServiceJourneyServiceException(e, caseDetails.getCaseId());
        }
    }

    @Override
    public CcdCallbackResponse serviceDecisionRefusal(CaseDetails caseDetails, String authorisation)
        throws ServiceJourneyServiceException {
        try {
            return CcdCallbackResponse.builder()
                .data(serviceDecisionMakingWorkflow.run(caseDetails, authorisation))
                .build();
        } catch (WorkflowException e) {
            throw new ServiceJourneyServiceException(e, caseDetails.getCaseId());
        }
    }

    @Override
    public Map<String, Object> setupConfirmServicePaymentEvent(CaseDetails caseDetails)
        throws ServiceJourneyServiceException {
        try {
            return setupConfirmServicePaymentWorkflow.run(caseDetails);
        } catch (WorkflowException exception) {
            throw new ServiceJourneyServiceException(exception, caseDetails.getCaseId());
        }
    }

    @Override
    public CcdCallbackResponse confirmServicePaymentEvent(CaseDetails caseDetails, String authorisation) throws ServiceJourneyServiceException {
        CcdCallbackResponse.CcdCallbackResponseBuilder builder = CcdCallbackResponse.builder();

        if (Conditions.isServiceApplicationBailiff(caseDetails.getCaseData())) {
            builder.state(AWAITING_BAILIFF_REFERRAL);
        } else {
            builder.state(AWAITING_SERVICE_CONSIDERATION);
        }

        try {
            builder.data(furtherPaymentWorkflow.run(caseDetails, getServiceApplicationPaymentType()));
        } catch (WorkflowException exception) {
            throw new ServiceJourneyServiceException(exception, caseDetails.getCaseId());
        }

        return builder.build();
    }
}
