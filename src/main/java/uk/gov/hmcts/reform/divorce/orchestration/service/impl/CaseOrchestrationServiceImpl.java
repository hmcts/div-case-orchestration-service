package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Payment;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AmendPetitionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AuthenticateRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CaseLinkedForHearingWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCallbackBulkPrintWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DNSubmittedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DeleteDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GetCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.IssueEventWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ProcessPbaPaymentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RespondentSubmittedCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SaveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendCoRespondSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerClarificationRequestNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerGenericEmailNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendRespondentSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SetOrderSummaryWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorCreateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitCoRespondentAosWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDnCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitRespondentAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseOrchestrationServiceImpl implements CaseOrchestrationService {
    private static final String PAYMENT_MADE = "paymentMade";
    private static final String SUCCESS = "success";
    private static final String ONLINE = "online";
    private static final String PAYMENT = "payment";

    private final IssueEventWorkflow issueEventWorkflow;
    private final CcdCallbackBulkPrintWorkflow ccdCallbackBulkPrintWorkflow;
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
    private final SendPetitionerGenericEmailNotificationWorkflow sendPetitionerGenericEmailNotificationWorkflow;
    private final SendPetitionerClarificationRequestNotificationWorkflow sendPetitionerClarificationRequestNotificationWorkflow;
    private final SendRespondentSubmissionNotificationWorkflow sendRespondentSubmissionNotificationWorkflow;
    private final SendCoRespondSubmissionNotificationWorkflow sendCoRespondSubmissionNotificationWorkflow;
    private final RespondentSubmittedCallbackWorkflow aosRespondedWorkflow;
    private final SubmitRespondentAosCaseWorkflow submitRespondentAosCaseWorkflow;
    private final SubmitCoRespondentAosWorkflow submitCoRespondentAosWorkflow;
    private final SubmitDnCaseWorkflow submitDnCaseWorkflow;
    private final DNSubmittedWorkflow dnSubmittedWorkflow;
    private final GetCaseWorkflow getCaseWorkflow;
    private final AuthUtil authUtil;
    private final AmendPetitionWorkflow amendPetitionWorkflow;
    private final CaseLinkedForHearingWorkflow caseLinkedForHearingWorkflow;

    @Override
    public Map<String, Object> handleIssueEventCallback(CcdCallbackRequest ccdCallbackRequest,
                                                        String authToken,
                                                        boolean generateAosInvitation) throws WorkflowException {
        Map<String, Object> payLoad = issueEventWorkflow.run(ccdCallbackRequest, authToken, generateAosInvitation);

        if (issueEventWorkflow.errors().isEmpty()) {
            log.info("Petition issued callback for case with CASE ID: {} successfully completed",
                ccdCallbackRequest.getCaseDetails().getCaseId());
            return payLoad;
        } else {
            log.error("Petition issued callback for case with CASE ID: {} failed",
                ccdCallbackRequest.getCaseDetails().getCaseId());
            return issueEventWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> ccdCallbackBulkPrintHandler(CcdCallbackRequest ccdCallbackRequest, String authToken)
        throws WorkflowException {

        Map<String, Object> payLoad = ccdCallbackBulkPrintWorkflow.run(ccdCallbackRequest, authToken);

        if (ccdCallbackBulkPrintWorkflow.errors().isEmpty()) {
            log.info("Bulk print callback for case with CASE ID: {} successfully completed",
                ccdCallbackRequest.getCaseDetails().getCaseId());
            return payLoad;
        } else {
            log.error("Bulk print callback for case with CASE ID: {} failed",
                ccdCallbackRequest.getCaseDetails().getCaseId());
            return ccdCallbackBulkPrintWorkflow.errors();
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
            log.info("Case with CASE ID: {} submitted", payload.get(ID));
            return payload;
        } else {
            log.info("Case with CASE ID: {} submit failed", payload.get(ID));
            return submitToCCDWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> update(Map<String, Object> divorceSession,
                                      String authToken,
                                      String caseId) throws WorkflowException {
        Map<String, Object> payload = updateToCCDWorkflow.run(divorceSession, authToken, caseId);

        log.info("Updated case with CASE ID: {}", payload.get(ID));
        return payload;
    }


    @Override
    public Map<String, Object> update(PaymentUpdate paymentUpdate) throws WorkflowException {
        Map<String, Object> payload = new HashMap<>();

        if (paymentUpdate.getStatus().equalsIgnoreCase(SUCCESS)) {
            String paymentAmount = Optional.ofNullable(paymentUpdate.getAmount())
                .map(BigDecimal::intValueExact)
                .map(amt -> amt * 100)
                .map(String::valueOf)
                .orElseThrow(() -> new WorkflowException("Missing payment amount data"));

            String feeId = Optional.ofNullable(paymentUpdate.getFees())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .orElseThrow(() -> new WorkflowException("Missing payment fee data"))
                .getCode();

            Payment payment = Payment.builder()
                .paymentChannel(ONLINE)
                .paymentReference(paymentUpdate.getReference())
                .paymentSiteId(paymentUpdate.getSiteId())
                .paymentStatus(paymentUpdate.getStatus())
                .paymentTransactionId(paymentUpdate.getExternalReference())
                .paymentAmount(paymentAmount)
                .paymentFeeId(feeId)
                .build();

            Map<String, Object> updateEvent = new HashMap<>();
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put(PAYMENT, payment);
            updateEvent.put(CASE_EVENT_DATA_JSON_KEY, sessionData);
            updateEvent.put(CASE_EVENT_ID_JSON_KEY, PAYMENT_MADE);

            payload = updateToCCDWorkflow.run(updateEvent,
                authUtil.getCaseworkerToken(), paymentUpdate.getCcdCaseNumber());
            log.info("Case ID is: {}. Payment updated with payment reference {}",
                payload.get(ID),
                payment.getPaymentReference());
        } else {
            log.info("Ignoring payment update as it was not successful payment on case {}",
                paymentUpdate.getCcdCaseNumber());
        }
        return payload;
    }


    @Override
    public Map<String, Object> getDraft(String authToken) throws WorkflowException {
        log.info("Returning draft");
        return retrieveDraftWorkflow.run(authToken);
    }

    @Override
    public Map<String, Object> saveDraft(Map<String, Object> payLoad,
                                         String authToken,
                                         String sendEmail) throws WorkflowException {
        Map<String, Object> response = saveDraftWorkflow.run(payLoad, authToken, sendEmail);

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
    public CaseDataResponse retrieveAosCase(String authorizationToken) throws WorkflowException {
        return retrieveAosCaseWorkflow.run(authorizationToken);
    }

    @Override
    public CaseDataResponse getCase(String authorizationToken) throws WorkflowException {
        return getCaseWorkflow.run(authorizationToken);
    }

    @Override
    public UserDetails linkRespondent(String authToken, String caseId, String pin) throws WorkflowException {
        return linkRespondentWorkflow.run(authToken, caseId, pin);
    }

    @Override
    public CcdCallbackResponse aosReceived(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        Map<String, Object> response = aosRespondedWorkflow.run(ccdCallbackRequest, authToken);
        log.info("Aos received notification completed with CASE ID: {}.",
            ccdCallbackRequest.getCaseDetails().getCaseId());

        if (aosRespondedWorkflow.errors().isEmpty()) {
            return CcdCallbackResponse.builder()
                .data(response)
                .build();
        } else {
            Map<String, Object> workflowErrors = aosRespondedWorkflow.errors();
            log.error("Aos received notification with CASE ID: {} failed." + workflowErrors,
                ccdCallbackRequest.getCaseDetails().getCaseId());
            return CcdCallbackResponse
                .builder()
                .errors(getNotificationErrors(workflowErrors))
                .build();
        }
    }

    @Override
    public CcdCallbackResponse sendCoRespReceivedNotificationEmail(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        Map<String, Object> response = sendCoRespondSubmissionNotificationWorkflow.run(ccdCallbackRequest);
        log.info("Co-respondent received notification completed with CASE ID: {}.",
            ccdCallbackRequest.getCaseDetails().getCaseId());

        if (sendCoRespondSubmissionNotificationWorkflow.errors().isEmpty()) {
            return CcdCallbackResponse.builder()
                .data(response)
                .build();
        } else {
            Map<String, Object> workflowErrors = sendCoRespondSubmissionNotificationWorkflow.errors();
            log.error("Co-respondent received notification with CASE ID: {} failed." + workflowErrors,
                ccdCallbackRequest.getCaseDetails().getCaseId());
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
        CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return sendPetitionerSubmissionNotificationWorkflow.run(ccdCallbackRequest);
    }

    @Override
    public Map<String, Object> sendPetitionerGenericUpdateNotificationEmail(
        CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return sendPetitionerGenericEmailNotificationWorkflow.run(ccdCallbackRequest);
    }

    @Override
    public Map<String, Object> sendPetitionerClarificationRequestNotification(final CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return sendPetitionerClarificationRequestNotificationWorkflow.run(ccdCallbackRequest);
    }

    @Override
    public Map<String, Object> sendRespondentSubmissionNotificationEmail(CcdCallbackRequest ccdCallbackRequest)
        throws WorkflowException {
        return sendRespondentSubmissionNotificationWorkflow.run(ccdCallbackRequest);
    }

    @Override
    public Map<String, Object> setOrderSummary(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return setOrderSummaryWorkflow.run(ccdCallbackRequest.getCaseDetails().getCaseData());
    }

    @Override
    public Map<String, Object> processPbaPayment(CcdCallbackRequest ccdCallbackRequest,
                                                 String authToken) throws WorkflowException {
        Map<String, Object> payLoad = processPbaPaymentWorkflow.run(ccdCallbackRequest, authToken);

        if (processPbaPaymentWorkflow.errors().isEmpty()) {
            log.info("Callback pay by account for solicitor with CASE ID: {} successfully completed",
                ccdCallbackRequest.getCaseDetails().getCaseId());
            return payLoad;
        } else {
            log.error("Callback pay by account for solicitor with CASE ID: {} failed. ",
                ccdCallbackRequest
                    .getCaseDetails()
                    .getCaseId());
            return processPbaPaymentWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> solicitorCreate(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return solicitorCreateWorkflow.run(ccdCallbackRequest.getCaseDetails().getCaseData());
    }

    @Override
    public Map<String, Object> submitRespondentAosCase(Map<String, Object> divorceSession, String authorizationToken,
                                                       String caseId)
        throws WorkflowException {
        Map<String, Object> payload = submitRespondentAosCaseWorkflow.run(divorceSession, authorizationToken, caseId);

        log.info("Updated respondent AOS with CASE ID: {}", payload.get(ID));
        return payload;
    }

    @Override
    public Map<String, Object> submitCoRespondentAosCase(final Map<String, Object> divorceSession, final String authorizationToken)
        throws WorkflowException {
        return submitCoRespondentAosWorkflow.run(divorceSession, authorizationToken);
    }

    @Override
    public Map<String, Object> submitDnCase(Map<String, Object> divorceSession, String authorizationToken,
                                            String caseId)
        throws WorkflowException {
        Map<String, Object> payload = submitDnCaseWorkflow.run(divorceSession, authorizationToken, caseId);

        log.info("Submitted DN with CASE ID: {}.", payload.get(ID));
        return payload;
    }

    @Override
    public CcdCallbackResponse dnSubmitted(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        Map<String, Object> response = dnSubmittedWorkflow.run(ccdCallbackRequest, authToken);

        if (dnSubmittedWorkflow.errors().isEmpty()) {
            log.info("CASE ID: {}. DN submitted notification sent.", ccdCallbackRequest
                .getCaseDetails()
                .getCaseId());
            return CcdCallbackResponse.builder()
                .data(response)
                .build();
        } else {
            Map<String, Object> workflowErrors = dnSubmittedWorkflow.errors();
            log.error("CASE ID: {}. DN submitted notification failed." + workflowErrors, ccdCallbackRequest
                .getCaseDetails()
                .getCaseId());
            return CcdCallbackResponse
                .builder()
                .errors(getNotificationErrors(workflowErrors))
                .build();
        }
    }

    @Override
    public Map<String, Object> amendPetition(String caseId, String authorisation) throws WorkflowException {
        Map<String, Object> response = amendPetitionWorkflow.run(caseId, authorisation);
        if (response != null) {
            log.info("Successfully created a new draft to amend, and updated old case {}", caseId);
        } else {
            log.error("Unable to create new amendment petition for case {}", caseId);
        }
        return response;
    }

    @Override
    public Map<String, Object> processCaseLinkedForHearingEvent(CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {
        try {
            return caseLinkedForHearingWorkflow.run(ccdCallbackRequest.getCaseDetails());
        } catch (WorkflowException e) {
            throw new CaseOrchestrationServiceException(e);
        }
    }

}