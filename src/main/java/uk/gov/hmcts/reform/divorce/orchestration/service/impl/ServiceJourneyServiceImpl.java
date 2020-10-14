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
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ReceivedServiceAddedDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceDecisionMadeWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceDecisionMakingWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceJourneyServiceImpl implements ServiceJourneyService {

    private final ReceivedServiceAddedDateWorkflow receivedServiceAddedDateWorkflow;
    private final MakeServiceDecisionWorkflow makeServiceDecisionWorkflow;
    private final ServiceDecisionMadeWorkflow serviceDecisionMadeWorkflow;
    private final ServiceDecisionMakingWorkflow serviceDecisionMakingWorkflow;

    @Override
    public CcdCallbackResponse makeServiceDecision(CaseDetails caseDetails, String authorisation) throws ServiceJourneyServiceException {
        CcdCallbackResponse.CcdCallbackResponseBuilder builder = CcdCallbackResponse.builder();

        if (isServiceApplicationGranted(caseDetails.getCaseData())) {
            builder.state(AWAITING_DECREE_NISI);
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
}
