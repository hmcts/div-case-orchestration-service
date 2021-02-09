package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.util.Map;

public interface GeneralReferralService {

    CcdCallbackResponse receiveReferral(CcdCallbackRequest ccdCallbackRequest)
        throws CaseOrchestrationServiceException;

    Map<String, Object> generalConsideration(CaseDetails caseDetails)
        throws CaseOrchestrationServiceException;

    Map<String, Object> setupGeneralReferralPaymentEvent(CaseDetails caseDetails)
        throws CaseOrchestrationServiceException;

    CcdCallbackResponse returnToStateBeforeGeneralReferral(CaseDetails caseDetails)
        throws CaseOrchestrationServiceException;

    Map<String, Object> generalReferralPaymentEvent(CaseDetails caseDetails)
        throws CaseOrchestrationServiceException;
}
