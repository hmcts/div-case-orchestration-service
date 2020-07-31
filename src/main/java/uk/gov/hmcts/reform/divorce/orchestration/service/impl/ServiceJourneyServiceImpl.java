package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ReceivedServiceAddedDateWorkflow;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceJourneyServiceImpl implements ServiceJourneyService {

    private final MakeServiceDecisionDateWorkflow makeServiceDecisionDateWorkflow;
    private final ReceivedServiceAddedDateWorkflow receivedServiceAddedDateWorkflow;

    @Override
    public CcdCallbackResponse makeServiceDecision(CaseDetails caseDetails) throws WorkflowException {
        CcdCallbackResponse.CcdCallbackResponseBuilder builder = CcdCallbackResponse.builder();

        Map<String, Object> caseData = makeServiceDecisionDateWorkflow.run(caseDetails);

        //        if (isServiceApplicationGranted(caseDetails)) {
        //            caseData.put(STATE_CCD_FIELD, AWAITING_DN_APPLICATION);
        //        } else {
        //            caseData.put(STATE_CCD_FIELD, SERVICE_APPLICATION_NOT_APPROVED);
        //        }

        builder.data(caseData);

        return builder.build();
    }

    @Override
    public Map<String, Object> receivedServiceAddedDate(CcdCallbackRequest ccdCallbackRequest)
        throws WorkflowException {
        return receivedServiceAddedDateWorkflow.run(ccdCallbackRequest.getCaseDetails());
    }

    //    protected boolean isServiceApplicationGranted(CaseDetails caseDetails) {
    //        return YES_VALUE.equalsIgnoreCase((String) caseDetails.getCaseData()
    //        .get(SERVICE_APPLICATION_GRANTED));
    //    }
}
