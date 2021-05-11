package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.util.Map;

public interface ServiceJourneyService {
    CcdCallbackResponse makeServiceDecision(CaseDetails caseDetails, String authorisation)
        throws ServiceJourneyServiceException;

    // TODO: Remove once ccd changes been deployed and tested
    Map<String, Object> receivedServiceAddedDate(CcdCallbackRequest ccdCallbackRequest) throws ServiceJourneyServiceException;

    CcdCallbackResponse serviceApplicationReceivedEvent(CaseDetails caseDetails)
        throws ServiceJourneyServiceException;

    CcdCallbackResponse serviceDecisionMade(CaseDetails caseDetails, String authorisation)
        throws ServiceJourneyServiceException;

    CcdCallbackResponse serviceDecisionRefusal(CaseDetails caseDetails, String authorisation)
        throws ServiceJourneyServiceException;

    Map<String, Object> setupConfirmServicePaymentEvent(CaseDetails caseDetails)
        throws ServiceJourneyServiceException;

    CcdCallbackResponse confirmServicePaymentEvent(CaseDetails caseDetails, String authorisation)
        throws ServiceJourneyServiceException;

    CcdCallbackResponse setupAddBailiffReturnEvent(CaseDetails caseDetails, String authorisation)
        throws ServiceJourneyServiceException;
}
