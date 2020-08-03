package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Payment;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AmendPetitionForRefusalWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AmendPetitionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AuthenticateRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.BulkCaseCancelPronouncementEventWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.BulkCaseRemoveCasesWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.BulkCaseUpdateDnPronounceDatesWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.BulkCaseUpdateHearingDetailsEventWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CaseLinkedForHearingWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCallbackBulkPrintWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CleanStatusCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CoRespondentAnswerReceivedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CreateNewAmendedCaseAndSubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DNSubmittedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DecreeNisiAboutToBeGrantedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DecreeNisiDecisionStateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DeleteDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GenerateCoRespondentAnswersWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GetCaseWithIdWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GetCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.IssueEventWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.MakeCaseEligibleForDecreeAbsoluteWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.PetitionerSolicitorRoleWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ProcessAwaitingPronouncementCasesWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveDNDocumentsWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveDnOutcomeCaseFlagWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveLegalAdvisorMakeDecisionFieldsWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveLinkFromListedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveLinkWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RespondentSolicitorLinkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RespondentSolicitorNominatedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RespondentSubmittedCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SaveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendClarificationSubmittedNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendCoRespondSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendDnPronouncedNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerClarificationRequestNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerEmailNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SeparationFieldsWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SetOrderSummaryWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorCreateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorDnFetchDocWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorSubmissionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorUpdateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitCoRespondentAosWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDaCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDnCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitRespondentAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ValidateBulkCaseListingWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.WelshContinueInterceptWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.WelshContinueWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.WelshSetPreviousStateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aos.AosSubmissionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.decreeabsolute.ApplicantDecreeAbsoluteEligibilityWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.decreeabsolute.DecreeAbsoluteAboutToBeGrantedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.DnSubmittedEmailNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.NotifyApplicantCanFinaliseDivorceWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.NotifyForRefusalOrderWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.SendDaGrantedNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.SendPetitionerAmendEmailNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ReceivedServiceAddedDateWorkflow;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.isPetitionerClaimingCosts;

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
    private final SolicitorSubmissionWorkflow solicitorSubmissionWorkflow;
    private final SolicitorCreateWorkflow solicitorCreateWorkflow;
    private final SolicitorUpdateWorkflow solicitorUpdateWorkflow;
    private final SendPetitionerSubmissionNotificationWorkflow sendPetitionerSubmissionNotificationWorkflow;
    private final SendPetitionerEmailNotificationWorkflow sendPetitionerEmailNotificationWorkflow;
    private final SendPetitionerClarificationRequestNotificationWorkflow sendPetitionerClarificationRequestNotificationWorkflow;
    private final AosSubmissionWorkflow aosSubmissionWorkflow;
    private final SendCoRespondSubmissionNotificationWorkflow sendCoRespondSubmissionNotificationWorkflow;
    private final RespondentSubmittedCallbackWorkflow aosRespondedWorkflow;
    private final SubmitRespondentAosCaseWorkflow submitRespondentAosCaseWorkflow;
    private final SubmitCoRespondentAosWorkflow submitCoRespondentAosWorkflow;
    private final SubmitDnCaseWorkflow submitDnCaseWorkflow;
    private final SubmitDaCaseWorkflow submitDaCaseWorkflow;
    private final DNSubmittedWorkflow dnSubmittedWorkflow;
    private final DnSubmittedEmailNotificationWorkflow dnSubmittedEmailNotificationWorkflow;
    private final SendDnPronouncedNotificationWorkflow sendDnPronouncedNotificationWorkflow;
    private final GetCaseWorkflow getCaseWorkflow;
    private final AuthUtil authUtil;
    private final AmendPetitionWorkflow amendPetitionWorkflow;
    private final AmendPetitionForRefusalWorkflow amendPetitionForRefusalWorkflow;
    private final CaseLinkedForHearingWorkflow caseLinkedForHearingWorkflow;
    private final CoRespondentAnswerReceivedWorkflow coRespondentAnswerReceivedWorkflow;
    private final ProcessAwaitingPronouncementCasesWorkflow processAwaitingPronouncementCasesWorkflow;
    private final GetCaseWithIdWorkflow getCaseWithIdWorkflow;
    private final SolicitorDnFetchDocWorkflow solicitorDnFetchDocWorkflow;
    private final GenerateCoRespondentAnswersWorkflow generateCoRespondentAnswersWorkflow;
    private final DocumentGenerationWorkflow documentGenerationWorkflow;
    private final RespondentSolicitorNominatedWorkflow respondentSolicitorNominatedWorkflow;
    private final SeparationFieldsWorkflow separationFieldsWorkflow;
    private final BulkCaseUpdateHearingDetailsEventWorkflow bulkCaseUpdateHearingDetailsEventWorkflow;
    private final BulkCaseCancelPronouncementEventWorkflow bulkCaseCancelPronouncementEventWorkflow;
    private final ValidateBulkCaseListingWorkflow validateBulkCaseListingWorkflow;
    private final RespondentSolicitorLinkCaseWorkflow respondentSolicitorLinkCaseWorkflow;
    private final DecreeNisiAboutToBeGrantedWorkflow decreeNisiAboutToBeGrantedWorkflow;
    private final DecreeNisiDecisionStateWorkflow decreeNisiDecisionStateWorkflow;
    private final SendPetitionerAmendEmailNotificationWorkflow sendPetitionerAmendEmailNotificationWorkflow;
    private final BulkCaseUpdateDnPronounceDatesWorkflow bulkCaseUpdateDnPronounceDatesWorkflow;
    private final CleanStatusCallbackWorkflow cleanStatusCallbackWorkflow;
    private final MakeCaseEligibleForDecreeAbsoluteWorkflow makeCaseEligibleForDecreeAbsoluteWorkflow;
    private final DecreeAbsoluteAboutToBeGrantedWorkflow decreeAbsoluteAboutToBeGrantedWorkflow;
    private final SendDaGrantedNotificationWorkflow sendDaGrantedNotificationWorkflow;
    private final ApplicantDecreeAbsoluteEligibilityWorkflow applicantDecreeAbsoluteEligibilityWorkflow;
    private final NotifyApplicantCanFinaliseDivorceWorkflow notifyApplicantCanFinaliseDivorceWorkflow;
    private final RemoveLinkWorkflow removeLinkWorkflow;
    private final BulkCaseRemoveCasesWorkflow bulkCaseRemoveCasesWorkflow;
    private final PetitionerSolicitorRoleWorkflow petitionerSolicitorRoleWorkflow;
    private final RemoveLinkFromListedWorkflow removeLinkFromListedWorkflow;
    private final RemoveDnOutcomeCaseFlagWorkflow removeDnOutcomeCaseFlagWorkflow;
    private final RemoveLegalAdvisorMakeDecisionFieldsWorkflow removeLegalAdvisorMakeDecisionFieldsWorkflow;
    private final WelshContinueWorkflow welshContinueWorkflow;
    private final WelshContinueInterceptWorkflow welshContinueInterceptWorkflow;
    private final WelshSetPreviousStateWorkflow welshSetPreviousStateWorkflow;
    private final NotifyForRefusalOrderWorkflow notifyForRefusalOrderWorkflow;
    private final RemoveDNDocumentsWorkflow removeDNDocumentsWorkflow;
    private final SendClarificationSubmittedNotificationWorkflow sendClarificationSubmittedNotificationWorkflow;
    private final CreateNewAmendedCaseAndSubmitToCCDWorkflow createNewAmendedCaseAndSubmitToCCDWorkflow;
    private final DocumentTemplateService documentTemplateService;
    private final ReceivedServiceAddedDateWorkflow receivedServiceAddedDateWorkflow;
    private final MakeServiceDecisionDateWorkflow makeServiceDecisionDateWorkflow;

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
    public Map<String, Object> ccdCallbackConfirmPersonalService(CcdCallbackRequest ccdCallbackRequest, String authToken)
        throws WorkflowException {

        Map<String, Object> payLoad = ccdCallbackRequest.getCaseDetails().getCaseData();
        String sendViaEmailOrPost = (String) payLoad.get(OrchestrationConstants.SEND_VIA_EMAIL_OR_POST);
        if (StringUtils.equalsIgnoreCase(sendViaEmailOrPost, OrchestrationConstants.SEND_VIA_POST)) {
            log.info("Confirm personal service callback for case with CASE ID: {} calling bulk print service",
                ccdCallbackRequest.getCaseDetails().getCaseId());
            payLoad = ccdCallbackBulkPrintHandler(ccdCallbackRequest, authToken);
        }
        return payLoad;
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
            CaseDetails caseDetails = getCaseWithIdWorkflow.run(paymentUpdate.getCcdCaseNumber());

            if (Objects.nonNull(caseDetails) && AWAITING_PAYMENT.equalsIgnoreCase(caseDetails.getState())) {
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
                    .paymentChannel(Optional.ofNullable(paymentUpdate.getChannel()).orElse(ONLINE))
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
                log.info("Ignoring payment update as the case is not in AwaitingPayment state case {}",
                    paymentUpdate.getCcdCaseNumber());
            }
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

    @Override
    public Map<String, Object> aosSubmission(CcdCallbackRequest ccdCallbackRequest, String authToken)
        throws WorkflowException {
        return aosSubmissionWorkflow.run(ccdCallbackRequest, authToken);
    }

    @Override
    public Map<String, Object> decreeNisiDecisionState(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return decreeNisiDecisionStateWorkflow.run(ccdCallbackRequest.getCaseDetails());
    }

    @Override
    public Map<String, Object> sendAmendApplicationEmail(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return sendPetitionerAmendEmailNotificationWorkflow.run(ccdCallbackRequest);
    }

    private List<String> getNotificationErrors(Map<String, Object> notificationErrors) {
        return notificationErrors.entrySet()
            .stream()
            .map(entry -> entry.getValue().toString())
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> sendPetitionerSubmissionNotificationEmail(CcdCallbackRequest ccdCallbackRequest)
        throws CaseOrchestrationServiceException {
        try {
            return sendPetitionerSubmissionNotificationWorkflow.run(ccdCallbackRequest);
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception, ccdCallbackRequest.getCaseDetails().getCaseId());
        }
    }

    @Override
    public Map<String, Object> sendPetitionerGenericUpdateNotificationEmail(
        CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return sendPetitionerEmailNotificationWorkflow.run(ccdCallbackRequest);
    }

    @Override
    public Map<String, Object> sendPetitionerClarificationRequestNotification(final CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return sendPetitionerClarificationRequestNotificationWorkflow.run(ccdCallbackRequest);
    }

    @Override
    public Map<String, Object> sendDnPronouncedNotification(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        return sendDnPronouncedNotificationWorkflow.run(ccdCallbackRequest.getCaseDetails(), authToken);
    }

    @Override
    public CcdCallbackResponse sendClarificationSubmittedNotificationEmail(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        Map<String, Object> workflowResponse = sendClarificationSubmittedNotificationWorkflow.run(ccdCallbackRequest);

        final String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        if (sendClarificationSubmittedNotificationWorkflow.errors().isEmpty()) {
            log.info("Clarification submitted notification for CASE ID: {} successfully completed", caseId);
            return CcdCallbackResponse.builder()
                .data(workflowResponse)
                .build();
        } else {
            log.error("Clarification submitted notification for  CASE ID: {} failed. ", caseId);
            List<String> errors = sendClarificationSubmittedNotificationWorkflow.errors().values().stream()
                .map(x -> (String) x)
                .collect(Collectors.toList());
            return CcdCallbackResponse.builder()
                .errors(errors)
                .build();
        }
    }

    @Override
    public CcdCallbackResponse setOrderSummaryAssignRole(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        Map<String, Object> updatedCase = setOrderSummaryWorkflow.run(ccdCallbackRequest.getCaseDetails().getCaseData());
        ccdCallbackRequest.getCaseDetails().setCaseData(updatedCase);
        Map<String, Object> solicitorPayload = petitionerSolicitorRoleWorkflow.run(ccdCallbackRequest, authToken);

        if (petitionerSolicitorRoleWorkflow.errors().isEmpty()) {
            log.info("Callback to assign [PETSOLICITOR] role with CASE ID: {} successfully completed", caseId);
            return CcdCallbackResponse.builder()
                .data(solicitorPayload)
                .build();
        } else {
            log.error("Callback to assign [PETSOLICITOR] role with CASE ID: {} failed. ", caseId);
            List<String> errors = petitionerSolicitorRoleWorkflow.errors().values().stream()
                .map(x -> (String) x)
                .collect(Collectors.toList());
            return CcdCallbackResponse.builder()
                .errors(errors)
                .build();
        }
    }

    @Override
    public Map<String, Object> solicitorSubmission(CcdCallbackRequest ccdCallbackRequest,
                                                   String authToken) throws WorkflowException {
        Map<String, Object> payLoad = solicitorSubmissionWorkflow.run(ccdCallbackRequest, authToken);

        if (solicitorSubmissionWorkflow.errors().isEmpty()) {
            log.info("Callback pay by account for solicitor with CASE ID: {} successfully completed",
                ccdCallbackRequest.getCaseDetails().getCaseId());
            return payLoad;
        } else {
            log.error("Callback pay by account for solicitor with CASE ID: {} failed. ",
                ccdCallbackRequest
                    .getCaseDetails()
                    .getCaseId());
            return solicitorSubmissionWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> solicitorCreate(CcdCallbackRequest ccdCallbackRequest, String authorizationToken)
        throws WorkflowException {
        return solicitorCreateWorkflow.run(ccdCallbackRequest.getCaseDetails(), authorizationToken);
    }

    @Override
    public Map<String, Object> solicitorUpdate(CcdCallbackRequest ccdCallbackRequest, String authorizationToken)
        throws WorkflowException {
        return solicitorUpdateWorkflow.run(ccdCallbackRequest.getCaseDetails(), authorizationToken);
    }

    @Override
    public Map<String, Object> solicitorAmendPetitionForRefusal(CcdCallbackRequest ccdCallbackRequest, String authorizationToken)
        throws WorkflowException {
        return createNewAmendedCaseAndSubmitToCCDWorkflow.run(ccdCallbackRequest.getCaseDetails(), authorizationToken);
    }

    @Override
    public Map<String, Object> submitRespondentAosCase(
        Map<String, Object> divorceSession, String authorizationToken, String caseId) throws WorkflowException {
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
    public Map<String, Object> submitDaCase(Map<String, Object> divorceSession, String authorizationToken,
                                            String caseId)
        throws WorkflowException {
        Map<String, Object> payload = submitDaCaseWorkflow.run(divorceSession, authorizationToken, caseId);

        log.info("Submitted Decree Absolute with CASE ID: {}.", payload.get(ID));
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
    public Map<String, Object> handleDnSubmitted(CcdCallbackRequest ccdCallbackRequest)
        throws WorkflowException {

        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        String caseId = caseDetails.getCaseId();

        return dnSubmittedEmailNotificationWorkflow.run(caseId, caseDetails.getCaseData());
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
    public Map<String, Object> amendPetitionForRefusal(String caseId, String authorisation) throws WorkflowException {
        Map<String, Object> response = amendPetitionForRefusalWorkflow.run(caseId, authorisation);
        if (response != null) {
            log.info("Successfully created a new draft to amend for DN Refusal, and updated old case {}", caseId);
        } else {
            log.error("Unable to create new DN Refusal amendment petition for case {}", caseId);
        }
        return response;
    }

    @Override
    public Map<String, Object> processCaseLinkedForHearingEvent(CcdCallbackRequest ccdCallbackRequest, String authToken)
        throws CaseOrchestrationServiceException {
        try {
            return caseLinkedForHearingWorkflow.run(ccdCallbackRequest.getCaseDetails(), authToken);
        } catch (WorkflowException e) {
            throw new CaseOrchestrationServiceException(e);
        }
    }

    @Override
    public Map<String, Object> processSolDnDoc(CcdCallbackRequest ccdCallbackRequest, final String documentType, final String docLinkFieldName)
        throws CaseOrchestrationServiceException {
        try {
            return solicitorDnFetchDocWorkflow.run(ccdCallbackRequest.getCaseDetails(), documentType, docLinkFieldName);
        } catch (WorkflowException e) {
            throw new CaseOrchestrationServiceException(e);
        }
    }

    @Override
    public Map<String, Object> processAosSolicitorNominated(CcdCallbackRequest ccdCallbackRequest,
                                                            String authToken) throws CaseOrchestrationServiceException {
        try {
            return respondentSolicitorNominatedWorkflow.run(ccdCallbackRequest.getCaseDetails(), authToken);
        } catch (WorkflowException e) {
            throw new CaseOrchestrationServiceException(e);
        }
    }

    @Override
    public Map<String, Object> processAosSolicitorLinkCase(CcdCallbackRequest request, String authToken) throws CaseOrchestrationServiceException {
        try {
            respondentSolicitorLinkCaseWorkflow.run(request.getCaseDetails(), authToken);
            return request.getCaseDetails().getCaseData();
        } catch (WorkflowException e) {
            throw new CaseOrchestrationServiceException(e);
        }
    }

    @Override
    public Map<String, Object> coRespondentAnswerReceived(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        return coRespondentAnswerReceivedWorkflow.run(ccdCallbackRequest.getCaseDetails());

    }

    @Override
    public Map<String, Object> generateCoRespondentAnswers(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        return generateCoRespondentAnswersWorkflow.run(ccdCallbackRequest.getCaseDetails(), authToken);
    }

    @Override
    public Map<String, Object> generateBulkCaseForListing() throws WorkflowException {
        log.info("Starting Bulk listing generation");
        Map<String, Object> result = processAwaitingPronouncementCasesWorkflow.run(authUtil.getCaseworkerToken());
        log.info("Bulk listing generation completed");
        return result;
    }

    @Override
    public Map<String, Object> handleDocumentGenerationCallback(final CcdCallbackRequest ccdCallbackRequest, final String authToken,
                                                                final String templateId, final String documentType, final String filename)
        throws WorkflowException {

        return documentGenerationWorkflow.run(ccdCallbackRequest, authToken, templateId, documentType, filename);
    }

    @Override
    public Map<String, Object> handleDnPronouncementDocumentGeneration(final CcdCallbackRequest ccdCallbackRequest, final String authToken)
        throws WorkflowException {

        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        if (Objects.nonNull(caseData.get(BULK_LISTING_CASE_ID_FIELD))) {
            LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(caseData);
            String templateId = documentTemplateService.getTemplateId(languagePreference, DocumentType.DECREE_NISI_TEMPLATE_ID);
            caseData.putAll(documentGenerationWorkflow.run(ccdCallbackRequest, authToken,
                templateId, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI_FILENAME));

            if (isPetitionerClaimingCosts(caseData)) {
                templateId = documentTemplateService.getTemplateId(languagePreference, DocumentType.COSTS_ORDER_TEMPLATE_ID);

                // DocumentType is clear enough to use as the file name
                caseData.putAll(documentGenerationWorkflow.run(ccdCallbackRequest, authToken,
                    templateId, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_DOCUMENT_TYPE));
            }
        }

        return caseData;
    }

    @Override
    public Map<String, Object> handleGrantDACallback(final CcdCallbackRequest ccdCallbackRequest, String authToken)
        throws WorkflowException {

        return decreeAbsoluteAboutToBeGrantedWorkflow.run(ccdCallbackRequest, authToken);
    }

    @Override
    public Map<String, Object> handleDaGranted(CcdCallbackRequest ccdCallbackRequest, String authorisationToken)
        throws WorkflowException {

        return sendDaGrantedNotificationWorkflow.run(ccdCallbackRequest.getCaseDetails(), authorisationToken);
    }

    @Override
    public Map<String, Object> processSeparationFields(CcdCallbackRequest ccdCallbackRequest)
        throws WorkflowException {

        Map<String, Object> payLoad = separationFieldsWorkflow.run(ccdCallbackRequest.getCaseDetails().getCaseData());

        if (separationFieldsWorkflow.errors().isEmpty()) {
            return payLoad;
        }
        return separationFieldsWorkflow.errors();
    }

    @Override
    public Map<String, Object> processBulkCaseScheduleForHearing(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        String bulkCaseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.info("Starting Bulk Schedule For Listing Callback on Bulk Case {}", bulkCaseId);
        Map<String, Object> result = bulkCaseUpdateHearingDetailsEventWorkflow.run(ccdCallbackRequest, authToken);
        log.info("Bulk Scheduling Successfully Initiated on Bulk Case {}", bulkCaseId);
        return result;
    }

    @Override
    public Map<String, Object> processCancelBulkCasePronouncement(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        String bulkCaseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.info("Starting Bulk Schedule cancel pronouncement for bulk with case id{}", bulkCaseId);
        Map<String, Object> result = bulkCaseCancelPronouncementEventWorkflow.run(ccdCallbackRequest, authToken);
        log.info("Bulk cancel pronouncement Successfully Initiated on Bulk Case {}", bulkCaseId);
        return result;
    }

    @Override
    public Map<String, Object> validateBulkCaseListingData(Map<String, Object> caseData) throws WorkflowException {
        return validateBulkCaseListingWorkflow.run(caseData);
    }

    @Override
    public Map<String, Object> editBulkCaseListingData(CcdCallbackRequest ccdCallbackRequest, String fileName,
                                                       String templateId, String documentType, String authToken) throws WorkflowException {
        Map<String, Object> response = validateBulkCaseListingWorkflow.run(ccdCallbackRequest.getCaseDetails().getCaseData());
        String judgeName = (String) ccdCallbackRequest.getCaseDetails().getCaseData().get(PRONOUNCEMENT_JUDGE_CCD_FIELD);
        if (StringUtils.isNotEmpty(judgeName)) {
            response = documentGenerationWorkflow.run(ccdCallbackRequest, authToken, templateId, documentType, fileName);
        }

        return response;
    }

    @Override
    public Map<String, Object> processCaseBeforeDecreeNisiIsGranted(CcdCallbackRequest ccdCallbackRequest, String authToken)
        throws CaseOrchestrationServiceException {

        try {
            return decreeNisiAboutToBeGrantedWorkflow.run(ccdCallbackRequest.getCaseDetails(), authToken);
        } catch (WorkflowException e) {
            throw new CaseOrchestrationServiceException(e);
        }
    }

    @Override
    public Map<String, Object> updateBulkCaseDnPronounce(CaseDetails caseDetails, String authToken) throws WorkflowException {
        return bulkCaseUpdateDnPronounceDatesWorkflow.run(caseDetails, authToken);
    }

    @Override
    public Map<String, Object> updateBulkCaseAcceptedCases(CaseDetails caseDetails, String authToken) throws WorkflowException {
        return bulkCaseRemoveCasesWorkflow.run(caseDetails, authToken);
    }

    @Override
    public Map<String, Object> cleanStateCallback(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {
        return cleanStatusCallbackWorkflow.run(callbackRequest, authToken);
    }

    @Override
    public Map<String, Object> makeCaseEligibleForDA(String authorisationToken, String caseId) throws CaseOrchestrationServiceException {
        Map<String, Object> returnedPayload;

        try {
            returnedPayload = makeCaseEligibleForDecreeAbsoluteWorkflow.run(authorisationToken, caseId);
            log.info("Case id {} made eligible for DA.", caseId);
        } catch (WorkflowException e) {
            log.error(format("Error occurred making case id %s eligible for DA.", caseId), e);
            throw new CaseOrchestrationServiceException(e);
        }

        return returnedPayload;
    }

    @Override
    public Map<String, Object> processApplicantDecreeAbsoluteEligibility(CcdCallbackRequest ccdCallbackRequest)
        throws CaseOrchestrationServiceException {

        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        String caseId = caseDetails.getCaseId();

        try {
            return applicantDecreeAbsoluteEligibilityWorkflow.run(caseId, caseDetails.getCaseData());
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception);
        }
    }

    @Override
    public Map<String, Object> handleMakeCaseEligibleForDaSubmitted(CcdCallbackRequest ccdCallbackRequest)
        throws WorkflowException {

        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        String caseId = caseDetails.getCaseId();

        return notifyApplicantCanFinaliseDivorceWorkflow.run(caseId, caseDetails.getCaseData());
    }

    @Override
    public Map<String, Object> removeBulkLink(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        return removeLinkWorkflow.run(caseDetails.getCaseData());
    }

    @Override
    public Map<String, Object> removeBulkListed(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        return removeLinkFromListedWorkflow.run(caseDetails.getCaseData());
    }

    @Override
    public Map<String, Object> removeDnOutcomeCaseFlag(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return removeDnOutcomeCaseFlagWorkflow.run(ccdCallbackRequest);
    }

    @Override
    public Map<String, Object> removeLegalAdvisorMakeDecisionFields(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return removeLegalAdvisorMakeDecisionFieldsWorkflow.run(ccdCallbackRequest);
    }

    @Override
    public Map<String, Object> welshContinue(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return welshContinueWorkflow.run(ccdCallbackRequest, authUtil.getCaseworkerToken());
    }

    @Override
    public Map<String, Object> notifyForRefusalOrder(CcdCallbackRequest ccdCallbackRequest)
        throws WorkflowException {

        return notifyForRefusalOrderWorkflow.run(ccdCallbackRequest.getCaseDetails());
    }

    @Override
    public Map<String, Object> removeDNGrantedDocuments(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return removeDNDocumentsWorkflow.run(ccdCallbackRequest.getCaseDetails().getCaseData());
    }

    @Override
    public CcdCallbackResponse welshContinueIntercept(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        Map<String, Object> response = welshContinueInterceptWorkflow.run(ccdCallbackRequest, authToken);
        log.info("welshContinueIntercept completed with CASE ID: {}.",
            ccdCallbackRequest.getCaseDetails().getCaseId());

        if (welshContinueInterceptWorkflow.errors().isEmpty()) {
            return CcdCallbackResponse.builder()
                .data(response)
                .build();
        } else {
            Map<String, Object> workflowErrors = welshContinueInterceptWorkflow.errors();
            log.error("welshContinueInterceptWorkflow with CASE ID: {} failed {}.",
                ccdCallbackRequest.getCaseDetails().getCaseId(), workflowErrors);
            return CcdCallbackResponse
                .builder()
                .errors(workflowErrors.values().stream().map(String.class::cast).collect(Collectors.toList()))
                .build();
        }
    }

    @Override
    public CcdCallbackResponse welshSetPreviousState(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        Map<String, Object> response = welshSetPreviousStateWorkflow.run(ccdCallbackRequest, authUtil.getCaseworkerToken());

        if (welshSetPreviousStateWorkflow.errors().isEmpty()) {
            return CcdCallbackResponse.builder()
                .data(response)
                .build();
        } else {
            Map<String, Object> workflowErrors = welshSetPreviousStateWorkflow.errors();
            log.error("CASE ID: {} failed {}. ",
                ccdCallbackRequest.getCaseDetails().getCaseId(), workflowErrors);
            return CcdCallbackResponse
                .builder()
                .errors(workflowErrors.values().stream().map(String.class::cast).collect(Collectors.toList()))
                .build();
        }
    }
}
