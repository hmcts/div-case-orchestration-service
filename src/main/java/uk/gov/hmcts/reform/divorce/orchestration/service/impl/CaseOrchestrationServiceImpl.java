package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Payment;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AuthenticateRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCallbackBulkPrintWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DNSubmittedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DeleteDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GetCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ProcessPbaPaymentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RespondentSubmittedCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SaveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerGenericEmailNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendRespondentSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SetOrderSummaryWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorCreateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDnCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;

import java.math.BigDecimal;
import java.util.Collections;
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
    private static final String CASE_ID_IS = "Case ID is: {}";
    private static final String PAYMENT_MADE = "paymentMade";
    private static final String SUCCESS = "success";
    private static final String ONLINE = "online";
    private static final String PAYMENT = "payment";

    private final CcdCallbackWorkflow ccdCallbackWorkflow;
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
    private final SendRespondentSubmissionNotificationWorkflow sendRespondentSubmissionNotificationWorkflow;
    private final RespondentSubmittedCallbackWorkflow aosRespondedWorkflow;
    private final SubmitAosCaseWorkflow submitAosCaseWorkflow;
    private final SubmitDnCaseWorkflow submitDnCaseWorkflow;
    private final DNSubmittedWorkflow dnSubmittedWorkflow;
    private final GetCaseWorkflow getCaseWorkflow;
    private final AuthUtil authUtil;

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
    public Map<String, Object> ccdCallbackBulkPrintHandler(CreateEvent caseDetailsRequest, String authToken)
        throws WorkflowException {

        Map<String, Object> payLoad = ccdCallbackBulkPrintWorkflow.run(caseDetailsRequest, authToken);

        if (ccdCallbackBulkPrintWorkflow.errors().isEmpty()) {
            log.info("Callback for case with id: {} successfully completed", payLoad.get(ID));
            return payLoad;
        } else {
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
    public Map<String, Object> update(PaymentUpdate paymentUpdate) throws WorkflowException {
        Map<String, Object> payload  = new HashMap<>();

        if (paymentUpdate.getStatus().equalsIgnoreCase(SUCCESS)) {
            Payment payment = Payment.builder()
                .paymentChannel(ONLINE)
                .paymentDate(paymentUpdate.getDateCreated())
                .paymentReference(paymentUpdate.getPaymentReference())
                .paymentSiteId(paymentUpdate.getSiteId())
                .paymentStatus(paymentUpdate.getStatus())
                .paymentTransactionId(paymentUpdate.getExternalReference())
                .build();

            Optional.ofNullable(paymentUpdate.getAmount())
                .map(BigDecimal::intValueExact)
                .map(amt -> amt * 100)
                .map(String::valueOf)
                .ifPresent(payment::setPaymentAmount);

            Optional.ofNullable(paymentUpdate.getFees())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .ifPresent(fee -> payment.setPaymentFeeId(fee.getCode()));

            Map<String, Object> updateEvent = new HashMap<>();
            updateEvent.put(CASE_EVENT_DATA_JSON_KEY, Collections.singletonMap(PAYMENT, payment));
            updateEvent.put(CASE_EVENT_ID_JSON_KEY, PAYMENT_MADE);

            payload = updateToCCDWorkflow.run(updateEvent,
                    authUtil.getCaseworkerToken(), paymentUpdate.getCcdCaseNumber());
            log.info("Case ID is: {}", payload.get(ID));
        } else  {
            log.info("Ignoring payment update as it was not successful payment on case {}",
                paymentUpdate.getCcdCaseNumber());
        }
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
    public CaseDataResponse getCase(String authorizationToken) throws WorkflowException {
        return getCaseWorkflow.run(authorizationToken);
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
    public Map<String, Object> sendPetitionerGenericUpdateNotificationEmail(
            CreateEvent caseDetailsRequest) throws WorkflowException {
        return sendPetitionerGenericEmailNotificationWorkflow.run(caseDetailsRequest);
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
