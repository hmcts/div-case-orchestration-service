package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface CaseOrchestrationService {

    Map<String, Object> ccdCallbackHandler(CreateEvent caseDetailsRequest, String authToken) throws WorkflowException;

    Boolean authenticateRespondent(String authToken) throws WorkflowException;

    /**
     * Submit case.
     */
    Map<String, Object> submit(Map<String, Object> divorceSession, String authToken) throws WorkflowException;

    /**
     * Update case.
     */
    Map<String, Object> update(Map<String, Object> divorceEventSession,
                               String authToken, String caseId) throws WorkflowException;

    CaseDataResponse retrieveAosCase(boolean checkCcd, String authorizationToken) throws WorkflowException;

    CcdCallbackResponse aosReceived(CreateEvent caseDetailsRequest, String authToken) throws WorkflowException;


    Map<String, Object> getDraft(String authToken) throws WorkflowException;

    Map<String,Object> saveDraft(Map<String, Object> payLoad,
                                 String authorizationToken,
                                 String notificationEmail) throws WorkflowException;

    Map<String,Object> deleteDraft(String authorizationToken) throws WorkflowException;

    /**
     * Sends notification email for successful submission.
     */
    Map<String, Object> sendSubmissionNotificationEmail(CreateEvent caseDetailsRequest) throws WorkflowException;

    /**
     * Get fee for petition issue and set it on the case data.
     */
    Map<String, Object> setOrderSummary(CreateEvent caseDetailsRequest) throws WorkflowException;

    /**
     * Process Pay By Account payment for Solicitor.
     */
    Map<String, Object> processPbaPayment(CreateEvent caseDetailsRequest, String authToken) throws WorkflowException;

    /**
     * Set Court details for Solicitor created case.
     */
    Map<String, Object> solicitorCreate(CreateEvent caseDetailsRequest) throws WorkflowException;
}
