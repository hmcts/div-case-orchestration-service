package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface ServiceJourneyService {
    CcdCallbackResponse makeServiceDecision(CaseDetails caseDetails, String authorisation) throws WorkflowException;

    Map<String, Object> receivedServiceAddedDate(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException;

    CcdCallbackResponse serviceDecisionMade(CaseDetails caseDetails, String authorisation, String decision) throws CaseOrchestrationServiceException;
}
