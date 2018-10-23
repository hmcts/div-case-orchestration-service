package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AuthenticateRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DNSubmittedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DeleteDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ProcessPbaPaymentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RespondentSubmittedCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SaveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendRespondentSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SetOrderSummaryWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorCreateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDnCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
public class CaseOrchestrationServiceImpl implements CaseOrchestrationService {
    public static final String CASE_ID_IS = "Case ID is: {}";
    private final CcdCallbackWorkflow ccdCallbackWorkflow;
    private final RetrieveDraftWorkflow retrieveDraftWorkflow;
    private final SaveDraftWorkflow saveDraftWorkflow;
    private final DeleteDraftWorkflow deleteDraftWorkflow;
    private final AuthenticateRespondentWorkflow authenticateRespondentWorkflow;
    private final SubmitToCCDWorkflow submitToCCDWorkflow;
    private final UpdateToCCDWorkflow updateToCCDWorkflow;
    private final RetrieveAosCaseWorkflow retrieveAosCaseWorkflow;
    private final LinkRespondentWorkflow linkRespondentWorkflow;
    private final SetOrderSummaryWorkflow setOrderSummaryWorkflow;
    private final ProcessPbaPaymentWorkflow processPbaPaymentWorkflow;
    private final SolicitorCreateWorkflow solicitorCreateWorkflow;
    private final SendPetitionerSubmissionNotificationWorkflow sendPetitionerSubmissionNotificationWorkflow;
    private final SendRespondentSubmissionNotificationWorkflow sendRespondentSubmissionNotificationWorkflow;
    private final RespondentSubmittedCallbackWorkflow aosRespondedWorkflow;
    private final SubmitAosCaseWorkflow submitAosCaseWorkflow;
    private final SubmitDnCaseWorkflow submitDnCaseWorkflow;
    private final DNSubmittedWorkflow dnSubmittedWorkflow;

    @Autowired
    public CaseOrchestrationServiceImpl(CcdCallbackWorkflow ccdCallbackWorkflow,
                                        AuthenticateRespondentWorkflow authenticateRespondentWorkflow,
                                        SubmitToCCDWorkflow submitToCCDWorkflow,
                                        UpdateToCCDWorkflow updateToCCDWorkflow,
                                        RetrieveAosCaseWorkflow retrieveAosCaseWorkflow,
                                        LinkRespondentWorkflow linkRespondentWorkflow,
                                        RetrieveDraftWorkflow retrieveDraftWorkflow,
                                        SaveDraftWorkflow saveDraftWorkflow,
                                        DeleteDraftWorkflow deleteDraftWorkflow,
                                        SetOrderSummaryWorkflow setOrderSummaryWorkflow,
                                        ProcessPbaPaymentWorkflow processPbaPaymentWorkflow,
                                        SolicitorCreateWorkflow solicitorCreateWorkflow,
                                        SendPetitionerSubmissionNotificationWorkflow
                                                    sendPetitionerSubmissionNotificationWorkflow,
                                        SendRespondentSubmissionNotificationWorkflow
                                                    sendRespondentSubmissionNotificationWorkflow,
                                        RespondentSubmittedCallbackWorkflow aosRespondedWorkflow,
                                        SubmitAosCaseWorkflow submitAosCaseWorkflow,
                                        DNSubmittedWorkflow submitDNWorkflow,
                                        SubmitDnCaseWorkflow submitDnCaseWorkflow) {
        this.ccdCallbackWorkflow = ccdCallbackWorkflow;
        this.authenticateRespondentWorkflow = authenticateRespondentWorkflow;
        this.submitToCCDWorkflow = submitToCCDWorkflow;
        this.updateToCCDWorkflow = updateToCCDWorkflow;
        this.retrieveDraftWorkflow = retrieveDraftWorkflow;
        this.saveDraftWorkflow = saveDraftWorkflow;
        this.deleteDraftWorkflow = deleteDraftWorkflow;
        this.retrieveAosCaseWorkflow = retrieveAosCaseWorkflow;
        this.linkRespondentWorkflow = linkRespondentWorkflow;
        this.aosRespondedWorkflow = aosRespondedWorkflow;
        this.setOrderSummaryWorkflow = setOrderSummaryWorkflow;
        this.processPbaPaymentWorkflow = processPbaPaymentWorkflow;
        this.solicitorCreateWorkflow = solicitorCreateWorkflow;
        this.sendPetitionerSubmissionNotificationWorkflow = sendPetitionerSubmissionNotificationWorkflow;
        this.sendRespondentSubmissionNotificationWorkflow = sendRespondentSubmissionNotificationWorkflow;
        this.submitAosCaseWorkflow = submitAosCaseWorkflow;
        this.submitDnCaseWorkflow = submitDnCaseWorkflow;
        this.dnSubmittedWorkflow = submitDNWorkflow;
    }

    @Override
    public Map<String, Object> ccdCallbackHandler(CreateEvent caseDetailsRequest,
                                                  String authToken,
                                                  boolean generateAosInvitation) throws WorkflowException {
        Map<String, Object> payLoad = ccdCallbackWorkflow.run(caseDetailsRequest, authToken, generateAosInvitation);

        if (ccdCallbackWorkflow.errors().isEmpty()) {
            log.info("Callback for case with id: {} successfully completed", payLoad.get(ID));
            return payLoad;
        } else {
            return ccdCallbackWorkflow.errors();
        }
    }

    @Override
    public Boolean authenticateRespondent(String authToken) throws WorkflowException {
        return authenticateRespondentWorkflow.run(authToken);
    }

    @Override
    public Map<String, Object> submit(Map<String, Object> divorceSession, String authToken) throws WorkflowException {
        Map<String, Object> payload = submitToCCDWorkflow.run(divorceSession, authToken);

        if (submitToCCDWorkflow.errors().isEmpty()) {
            log.info(CASE_ID_IS, payload.get(ID));
            return payload;
        } else {
            return submitToCCDWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> update(Map<String, Object> divorceSession,
                                      String authToken,
                                      String caseId) throws WorkflowException {
        Map<String, Object> payload = updateToCCDWorkflow.run(divorceSession, authToken, caseId);

        log.info("Case ID is: {}", payload.get(ID));
        return payload;
    }

    @Override
    public Map<String, Object> getDraft(String authToken, Boolean checkCcd) throws WorkflowException {
        return retrieveDraftWorkflow.run(authToken, checkCcd);
    }

    @Override
    public Map<String, Object> saveDraft(Map<String, Object> payLoad,
                                         String authToken,
                                         String notificationEmail) throws WorkflowException {
        Map<String, Object> response = saveDraftWorkflow.run(payLoad, authToken, notificationEmail);

        if (saveDraftWorkflow.errors().isEmpty()) {
            log.info("Draft saved");
            return response;
        } else {
            log.error("Workflow error saving draft");
            return saveDraftWorkflow.errors();
        }

    }

    @Override
    public Map<String, Object> deleteDraft(String authToken) throws WorkflowException {
        Map<String, Object> response = deleteDraftWorkflow.run(authToken);
        if (deleteDraftWorkflow.errors().isEmpty()) {
            log.info("Draft deleted");
            return response;
        } else {
            log.error("Workflow error deleting draft");
            return deleteDraftWorkflow.errors();
        }
    }

    @Override
    public CaseDataResponse retrieveAosCase(boolean checkCcd, String authorizationToken) throws WorkflowException {
        return retrieveAosCaseWorkflow.run(checkCcd, authorizationToken);
    }

    @Override
    public UserDetails linkRespondent(String authToken, String caseId, String pin) throws WorkflowException {
        return linkRespondentWorkflow.run(authToken, caseId, pin);
    }

    @Override
    public CcdCallbackResponse aosReceived(CreateEvent caseDetailsRequest, String authToken) throws WorkflowException {
        Map<String, Object> response = aosRespondedWorkflow.run(caseDetailsRequest, authToken);
        if (aosRespondedWorkflow.errors().isEmpty()) {
            return CcdCallbackResponse.builder()
                    .data(response)
                    .build();
        } else {
            Map<String, Object> workflowErrors = aosRespondedWorkflow.errors();
            log.error("Aos received notification failed." + workflowErrors);
            return CcdCallbackResponse
                    .builder()
                    .errors(getNotificationErrors(workflowErrors))
                    .build();
        }
    }

    private List<String> getNotificationErrors(Map<String, Object> notificationErrors) {
        return notificationErrors.entrySet()
                .stream()
                .map(entry -> entry.getValue().toString())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> sendPetitionerSubmissionNotificationEmail(
            CreateEvent caseDetailsRequest) throws WorkflowException {
        return sendPetitionerSubmissionNotificationWorkflow.run(caseDetailsRequest);
    }

    @Override
    public Map<String, Object> sendRespondentSubmissionNotificationEmail(CreateEvent caseDetailsRequest)
            throws WorkflowException {
        return sendRespondentSubmissionNotificationWorkflow.run(caseDetailsRequest);
    }

    @Override
    public Map<String, Object> setOrderSummary(CreateEvent caseDetailsRequest) throws WorkflowException {
        return setOrderSummaryWorkflow.run(caseDetailsRequest.getCaseDetails().getCaseData());
    }

    @Override
    public Map<String, Object> processPbaPayment(CreateEvent caseDetailsRequest,
                                                 String authToken) throws WorkflowException {
        Map<String, Object> payLoad = processPbaPaymentWorkflow.run(caseDetailsRequest, authToken);

        if (processPbaPaymentWorkflow.errors().isEmpty()) {
            log.info("Callback pay by account for solicitor case with id: {} successfully completed",
                    payLoad.get(ID));
            return payLoad;
        } else {
            return processPbaPaymentWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> solicitorCreate(CreateEvent caseDetailsRequest) throws WorkflowException {
        return solicitorCreateWorkflow.run(caseDetailsRequest.getCaseDetails().getCaseData());
    }

    @Override
    public Map<String, Object> submitAosCase(Map<String, Object> divorceSession, String authorizationToken,
                                             String caseId)
        throws WorkflowException {
        Map<String, Object> payload = submitAosCaseWorkflow.run(divorceSession, authorizationToken, caseId);

        log.info("Case ID is: {}", payload.get(ID));
        return payload;
    }

    @Override
    public Map<String, Object> submitDnCase(Map<String, Object> divorceSession, String authorizationToken,
                                             String caseId)
            throws WorkflowException {
        Map<String, Object> payload = submitDnCaseWorkflow.run(divorceSession, authorizationToken, caseId);

        log.info("Case ID is: {}", payload.get(ID));
        return payload;
    }

    @Override
    public CcdCallbackResponse dnSubmitted(CreateEvent caseDetailsRequest, String authToken) throws WorkflowException {
        Map<String, Object> response = dnSubmittedWorkflow.run(caseDetailsRequest, authToken);

        if (dnSubmittedWorkflow.errors().isEmpty()) {
            log.info("Case ID {}. DN submitted notification sent.", caseDetailsRequest
                    .getCaseDetails()
                    .getCaseId());
            return CcdCallbackResponse.builder()
                    .data(response)
                    .build();
        } else {
            Map<String, Object> workflowErrors = dnSubmittedWorkflow.errors();
            log.error("Case ID {}. DN submitted notification failed." + workflowErrors, caseDetailsRequest
                    .getCaseDetails()
                    .getCaseId());
            return CcdCallbackResponse
                    .builder()
                    .errors(getNotificationErrors(workflowErrors))
                    .build();
        }
    }
}
