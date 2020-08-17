package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyService;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ReceivedServiceAddedDateWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceJourneyServiceImpl implements ServiceJourneyService {

    private final MakeServiceDecisionDateWorkflow makeServiceDecisionDateWorkflow;
    private final ReceivedServiceAddedDateWorkflow receivedServiceAddedDateWorkflow;

    @Override
    public CcdCallbackResponse makeServiceDecision(CaseDetails caseDetails, String authorisation) throws ServiceJourneyServiceException {
        CcdCallbackResponse.CcdCallbackResponseBuilder builder = CcdCallbackResponse.builder();

        if (isServiceApplicationGranted(caseDetails)) {
            builder.state(AWAITING_DECREE_NISI);
        } else {
            builder.state(SERVICE_APPLICATION_NOT_APPROVED);
        }

        try {
            builder.data(makeServiceDecisionDateWorkflow.run(caseDetails, authorisation));
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
    public Map<String, Object> handleAwaitingServiceConsideration(CcdCallbackRequest ccdCallbackRequest) throws ServiceJourneyServiceException {
        return ccdCallbackRequest.getCaseDetails().getCaseData();
    }

    protected boolean isServiceApplicationGranted(CaseDetails caseDetails) {
        return YES_VALUE.equalsIgnoreCase(
            (String) caseDetails.getCaseData().get(CcdFields.SERVICE_APPLICATION_GRANTED)
        );
    }
}
