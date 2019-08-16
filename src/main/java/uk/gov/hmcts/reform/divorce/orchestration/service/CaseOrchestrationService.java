package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface CaseOrchestrationService {

    Map<String, Object> handleIssueEventCallback(CcdCallbackRequest ccdCallbackRequest, String authToken,
                                                 boolean generateAosInvitation) throws WorkflowException;

    Map<String, Object> ccdCallbackBulkPrintHandler(CcdCallbackRequest ccdCallbackRequest, String authToken)
        throws WorkflowException;

    Boolean authenticateRespondent(String authToken) throws WorkflowException;

    Map<String, Object> submit(Map<String, Object> divorceSession, String authToken) throws WorkflowException;

    Map<String, Object> update(Map<String, Object> divorceEventSession,
                               String authToken, String caseId) throws WorkflowException;

    Map<String, Object> update(PaymentUpdate paymentUpdate) throws WorkflowException;

    CaseDataResponse retrieveAosCase(String authorizationToken) throws WorkflowException;

    CaseDataResponse getCase(String authorizationToken) throws WorkflowException;

    UserDetails linkRespondent(String authToken, String caseId, String pin)
        throws WorkflowException;

    CcdCallbackResponse aosReceived(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException;


    Map<String, Object> getDraft(String authToken) throws WorkflowException;

    Map<String, Object> saveDraft(Map<String, Object> payLoad,
                                  String authorizationToken,
                                  String sendEmail) throws WorkflowException;

    Map<String, Object> deleteDraft(String authorizationToken) throws WorkflowException;

    Map<String, Object> sendPetitionerSubmissionNotificationEmail(CcdCallbackRequest ccdCallbackRequest)
        throws WorkflowException;

    Map<String, Object> sendPetitionerGenericUpdateNotificationEmail(CcdCallbackRequest ccdCallbackRequest)
        throws WorkflowException;

    Map<String, Object> sendRespondentSubmissionNotificationEmail(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException;

    Map<String, Object> sendPetitionerClarificationRequestNotification(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException;

    Map<String, Object> setOrderSummary(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException;

    Map<String, Object> solicitorSubmission(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException;

    Map<String, Object> solicitorCreate(CcdCallbackRequest ccdCallbackRequest, String authorizationToken)
        throws WorkflowException;

    Map<String, Object> solicitorUpdate(CcdCallbackRequest ccdCallbackRequest, String authorizationToken)
        throws WorkflowException;

    Map<String, Object> submitRespondentAosCase(Map<String, Object> payload, String authorizationToken, String caseId)
        throws WorkflowException;

    Map<String, Object> submitCoRespondentAosCase(Map<String, Object> payload, String authorizationToken)
        throws WorkflowException;

    CcdCallbackResponse dnSubmitted(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException;

    Map<String, Object> submitDnCase(Map<String, Object> divorceSession, String authorizationToken, String caseId)
        throws WorkflowException;

    Map<String, Object> submitDaCase(Map<String, Object> divorceSession, String authorizationToken, String caseId)
            throws WorkflowException;

    Map<String, Object> amendPetition(String caseId, String authorisation) throws WorkflowException;

    CcdCallbackResponse sendCoRespReceivedNotificationEmail(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException;

    Map<String, Object> sendDnPronouncedNotificationEmail(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException;

    Map<String, Object> processCaseLinkedForHearingEvent(CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException;

    Map<String, Object> coRespondentAnswerReceived(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException;

    Map<String, Object> processSolDnDoc(CcdCallbackRequest ccdCallbackRequest, String documentType, String docLinkFieldName)
        throws CaseOrchestrationServiceException;

    Map<String, Object> generateCoRespondentAnswers(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException;

    Map<String, Object> generateBulkCaseForListing() throws WorkflowException;

    Map<String, Object> handleDocumentGenerationCallback(CcdCallbackRequest ccdCallbackRequest, String authToken, String templateId,
                                                         String documentType, String filename) throws WorkflowException;

    Map<String, Object> processAosSolicitorNominated(CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException;

    Map<String, Object> processSeparationFields(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException;

    Map<String, Object> processBulkCaseScheduleForHearing(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException;

    Map<String, Object> validateBulkCaseListingData(Map<String, Object> caseData) throws WorkflowException;

    Map<String, Object> handleDnPronouncementDocumentGeneration(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException;

    Map<String, Object> processAosSolicitorLinkCase(CcdCallbackRequest request, String authToken) throws CaseOrchestrationServiceException;

    Map<String, Object> processCaseBeforeDecreeNisiIsGranted(CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException;

    Map<String, Object> updateBulkCaseDnPronounce(CaseDetails caseDetails, String authToken) throws WorkflowException;

    Map<String, Object> cleanStateCallback(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException;

    Map<String, Object> makeCaseEligibleForDA(String authorisationToken, String caseId) throws CaseOrchestrationServiceException;

    Map<String, Object> handleGrantDACallback(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException;

    Map<String, Object> processApplicantDecreeAbsoluteEligibility(CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException;

    Map<String, Object> removeBulkLink(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException;

    Map<String, Object> updateBulkCaseAcceptedCases(CaseDetails caseDetails, String authToken) throws WorkflowException;

    Map<String, Object> editBulkCaseListingData(CcdCallbackRequest ccdCallbackRequest, String fileName,
                                                String templateId, String documentType, String authToken) throws WorkflowException;

}
