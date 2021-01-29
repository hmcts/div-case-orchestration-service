package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.model.response.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AlternativeServiceService;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CourtOrderDocumentsUpdateService;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralReferralService;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyService;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_PRINT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_LIST_FOR_PRONOUNCEMENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_LIST_FOR_PRONOUNCEMENT_FILE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CERTIFICATE_OF_ENTITLEMENT_FILENAME_PREFIX;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_ANSWERS_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.GENERATE_AOS_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ANSWERS_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PBA_PAYMENT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.CASE_LIST_FOR_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ControllerUtils.getPbaUpdatedState;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ControllerUtils.getResponseErrors;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ControllerUtils.isPaymentSuccess;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ControllerUtils.isResponseErrors;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CallbackController {

    private static final String GENERIC_ERROR_MESSAGE = "An error happened when processing this request.";
    private static final String FAILED_TO_PROCESS_SOL_DN_ERROR = "Failed to process Solicitor DN review petition for case ID: %s";
    private static final String FAILED_TO_EXECUTE_SERVICE_ERROR = "Failed to execute service for case ID:  %s";

    private final CaseOrchestrationService caseOrchestrationService;
    private final ServiceJourneyService serviceJourneyService;
    private final GeneralOrderService generalOrderService;
    private final AosService aosService;
    private final GeneralEmailService generalEmailService;
    private final GeneralReferralService generalReferralService;
    private final AlternativeServiceService alternativeServiceService;
    private final CourtOrderDocumentsUpdateService courtOrderDocumentsUpdateService;

    @PostMapping(path = "/request-clarification-petitioner")
    @ApiOperation(value = "Trigger notification email to request clarification from Petitioner")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Petitioner clarification request sent successfully"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> requestClarificationFromPetitioner(
        @RequestBody @ApiParam("CaseData") final CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        caseOrchestrationService.sendPetitionerClarificationRequestNotification(ccdCallbackRequest);
        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build());
    }

    @PostMapping(path = "/dn-submitted")
    @ApiOperation(value = "Trigger notification email to Petitioner that their Decree Nisi submitted successfully")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "DN Submitted notification sent successful"),
        @ApiResponse(code = 401, message = "User Not Authenticated"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> dnSubmitted(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(caseOrchestrationService.dnSubmitted(ccdCallbackRequest, authorizationToken));
    }

    @PostMapping(path = "/handle-post-dn-submitted")
    @ApiOperation(value = "Callback to run after DN Submit event has finished")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "DN Submitted callback processed")})
    public ResponseEntity<CcdCallbackResponse> handleDnSubmitted(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(caseOrchestrationService.handleDnSubmitted(ccdCallbackRequest))
            .build());
    }

    @PostMapping(path = "/dn-pronounced", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Trigger notification email to Petitioner and Respondent when the Decree Nisi has been pronounced")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "An email notification has been generated and dispatched",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> dnPronounced(
        @RequestHeader(value = AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(caseOrchestrationService.sendDnPronouncedNotification(ccdCallbackRequest, authorizationToken));
            log.info("DN pronounced for case ID: {}.", caseId);
        } catch (WorkflowException exception) {
            log.error("DN pronounced handling has failed for case ID: {}", caseId, exception);
            callbackResponseBuilder.errors(singletonList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/clarification-submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Trigger notification email to Petitioner when their clarification has been submitted")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "An email notification has been generated and dispatched",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> clarificationSubmitted(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        CcdCallbackResponse workflowResponse = caseOrchestrationService.sendClarificationSubmittedNotificationEmail(ccdCallbackRequest);
        return ResponseEntity.ok(workflowResponse);
    }

    @PostMapping(path = "/petition-issue-fees",
        consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON)
    @ApiOperation(value = "Return an Order Summary for Petition Issue")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Petition issue fee amount is sent to CCD as callback response",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> getPetitionIssueFees(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(caseOrchestrationService.setOrderSummaryAssignRole(ccdCallbackRequest, authorizationToken));
    }

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/process-pba-payment", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to receive payment from the Solicitor")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Solicitor Pay callback successfully triggered",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> processPbaPayment(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        Map<String, Object> response = caseOrchestrationService.solicitorSubmission(ccdCallbackRequest, authorizationToken);

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        CcdCallbackResponse.CcdCallbackResponseBuilder responseBuilder = CcdCallbackResponse.builder();

        if (isResponseErrors(SOLICITOR_PBA_PAYMENT_ERROR_KEY, response)) {
            responseBuilder.errors(getResponseErrors(SOLICITOR_PBA_PAYMENT_ERROR_KEY, response));
        } else if (isPaymentSuccess(response)) {
            responseBuilder.state(getPbaUpdatedState(caseId, response));
            responseBuilder.data(response);
        } else {
            responseBuilder.state(ProcessPbaPaymentTask.DEFAULT_END_STATE_FOR_NON_PBA_PAYMENTS);
            responseBuilder.data(response);
        }

        log.info("CaseID {} Exiting /process-pba-payment to receive payment from the Solicitor", caseId);
        return ResponseEntity.ok(responseBuilder.build());
    }

    @PostMapping(path = "/solicitor-create", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to populate missing requirement fields when creating solicitor cases.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback to populate missing requirement fields when creating solicitor cases.",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> solicitorCreate(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(caseOrchestrationService.solicitorCreate(ccdCallbackRequest, authorizationToken))
            .build());
    }

    @PostMapping(path = "/solicitor-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Solicitor updated a case callback")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully handled Solicitor case update",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> solicitorUpdate(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(caseOrchestrationService.solicitorUpdate(ccdCallbackRequest, authorizationToken))
            .build());
    }

    @PostMapping(path = "/solicitor-amend-petition-dn-rejection", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to amend current petition and create a new case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully amended the current petition and created a new case.",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> solicitorAmendPetitionForRefusal(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(caseOrchestrationService.solicitorAmendPetitionForRefusal(ccdCallbackRequest, authorizationToken))
            .build());
    }

    @PostMapping(path = "/aos-submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Trigger notification email to Respondent that their AOS has been submitted")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Email sent to Respondent for AOS Submitted",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> respondentAOSSubmitted(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.info("/aos-submitted endpoint called for caseId {}", caseId);
        Map<String, Object> returnedCaseData;
        try {
            returnedCaseData = caseOrchestrationService.aosSubmission(ccdCallbackRequest, authorizationToken);
        } catch (WorkflowException e) {
            log.error("Failed to call service for caseId {}", caseId, e);
            return ResponseEntity.ok(CcdCallbackResponse.builder()
                .errors(singletonList(e.getMessage()))
                .build());
        }

        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(returnedCaseData)
            .build());
    }

    @PostMapping(path = "/petition-submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Trigger notification email to Petitioner when their application is submitted")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Email sent to Petitioner that their application has been submitted",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> petitionSubmitted(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(caseOrchestrationService.sendPetitionerSubmissionNotificationEmail(ccdCallbackRequest))
                .build());

    }

    @PostMapping(path = "/petition-updated", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Trigger notification email to Petitioner when their application is updated")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Email sent to Petitioner that their application has been updated",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> petitionUpdated(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.info("/petition-updated endpoint called for caseId {}", caseId);

        try {
            caseOrchestrationService.sendPetitionerGenericUpdateNotificationEmail(ccdCallbackRequest);
        } catch (WorkflowException e) {
            log.error("Failed to complete service for caseId {}", caseId, e);
            return ResponseEntity.ok(CcdCallbackResponse.builder()
                .data(ccdCallbackRequest.getCaseDetails().getCaseData())
                .errors(singletonList(e.getMessage()))
                .build());
        }

        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build());
    }

    @PostMapping(path = "/default-values",
        consumes = MediaType.APPLICATION_JSON,
        produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Default application state")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Default state set",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> defaultValue(
        @RequestHeader(value = "Authorization", required = false) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        ccdCallbackRequest.getCaseDetails().getCaseData().computeIfAbsent(LANGUAGE_PREFERENCE_WELSH, value -> NO_VALUE);

        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build());
    }

    @PostMapping(path = "/confirm-service",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Caseworker confirm personal service from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> confirmPersonalService(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        Map<String, Object> response = caseOrchestrationService.ccdCallbackConfirmPersonalService(ccdCallbackRequest,
            authorizationToken);

        if (response != null && response.containsKey(BULK_PRINT_ERROR_KEY)) {
            return ResponseEntity.ok(
                CcdCallbackResponse.builder()
                    .data(ImmutableMap.of())
                    .warnings(ImmutableList.of())
                    .errors(singletonList("Failed to bulk print documents"))
                    .build());
        }
        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(response)
                .errors(Collections.emptyList())
                .warnings(Collections.emptyList())
                .build());
    }

    @PostMapping(path = "/bulk-print", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Handles bulk print callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> bulkPrint(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        try {
            Map<String, Object> response = caseOrchestrationService.ccdCallbackBulkPrintHandler(
                ccdCallbackRequest, authorizationToken
            );

            if (response != null && response.containsKey(BULK_PRINT_ERROR_KEY)) {
                return ResponseEntity.ok(
                    CcdCallbackResponse.builder()
                        .data(ImmutableMap.of())
                        .warnings(ImmutableList.of())
                        .errors(singletonList("Failed to bulk print documents"))
                        .build());
            }
            return ResponseEntity.ok(
                CcdCallbackResponse.builder()
                    .data(response)
                    .errors(Collections.emptyList())
                    .warnings(Collections.emptyList())
                    .build());
        } catch (WorkflowException e) {
            return ResponseEntity.ok(
                CcdCallbackResponse.builder()
                    .data(ImmutableMap.of())
                    .warnings(ImmutableList.of())
                    .errors(singletonList("Failed to bulk print documents - " + e.getMessage()))
                    .build());
        }
    }

    @PostMapping(path = "/petition-issued", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Handles Issue event callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> petitionIssuedCallback(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestParam(value = GENERATE_AOS_INVITATION, required = false)
        @ApiParam(GENERATE_AOS_INVITATION) boolean generateAosInvitation,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        Map<String, Object> response = caseOrchestrationService.handleIssueEventCallback(ccdCallbackRequest, authorizationToken,
            generateAosInvitation);

        if (response != null && response.containsKey(VALIDATION_ERROR_KEY)) {
            return ResponseEntity.ok(
                CcdCallbackResponse.builder()
                    .errors(getErrors(response))
                    .build());
        }

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(response)
                .build());
    }

    @PostMapping(path = "/case-linked-for-hearing", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Handle actions that need to happen once the case has been linked for hearing (been given a hearing date).")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error, message is attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> caseLinkedForHearing(
        @RequestHeader(value = AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.debug("Processing case linked for hearing. Case id: {}", caseId);

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(
                caseOrchestrationService.processCaseLinkedForHearingEvent(ccdCallbackRequest, authorizationToken));
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format("Failed to execute service to process case linked for hearing. Case id:  %s", caseId),
                exception);
            callbackResponseBuilder.errors(ImmutableList.of(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/co-respondent-answered", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Set co-respondent answer received fields on Divorce case.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error, message is attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> respondentAnswerReceived(
        @RequestBody @ApiParam("CaseData")
            CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(caseOrchestrationService.coRespondentAnswerReceived(ccdCallbackRequest));
            log.info("Co-respondent answer received. Case id: {}", caseId);

        } catch (WorkflowException exception) {
            log.error("Co-respondent answer received failed. Case id:  {}", caseId, exception);
            callbackResponseBuilder.errors(asList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/sol-dn-review-petition", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Populates fields for solicitor DN journey")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully populated fields for solicitor DN journey",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> solDnReviewPetition(@RequestBody CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.debug("Processing Solicitor DN review petition for Case ID: {}", caseId);

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(
                caseOrchestrationService.processSolDnDoc(
                    ccdCallbackRequest,
                    DOCUMENT_TYPE_PETITION,
                    MINI_PETITION_LINK
                )
            );
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format(FAILED_TO_PROCESS_SOL_DN_ERROR, caseId), exception);
            callbackResponseBuilder.errors(ImmutableList.of(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/sol-dn-resp-answers-doc", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Populates Respondent Answers doc for solicitor DN journey")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully populated Respondent Answers doc for solicitor DN journey",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> solDnRespAnswersDoc(@RequestBody CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.debug("Processing solicitor DN Respondent Answers doc for Case ID: {}", caseId);

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(
                caseOrchestrationService.processSolDnDoc(
                    ccdCallbackRequest, DocumentType.RESPONDENT_ANSWERS.getTemplateLogicalName(),
                    RESP_ANSWERS_LINK
                )
            );
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format(FAILED_TO_PROCESS_SOL_DN_ERROR, caseId), exception);
            callbackResponseBuilder.errors(ImmutableList.of(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/sol-dn-co-resp-answers-doc", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Populates Co-Respondent Answers doc for solicitor DN journey")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully populated Co-Respondent Answers doc for solicitor DN journey",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> solDnCoRespAnswersDoc(@RequestBody CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.debug("Processing solicitor DN Co-Respondent Answers doc for Case ID: {}", caseId);

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(
                caseOrchestrationService.processSolDnDoc(
                    ccdCallbackRequest,
                    DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS,
                    CO_RESP_ANSWERS_LINK
                )
            );
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format(FAILED_TO_PROCESS_SOL_DN_ERROR, caseId), exception);
            callbackResponseBuilder.errors(ImmutableList.of(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/co-respondent-generate-answers", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Generates the Co-Respondent Answers PDF Document for the case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Co-Respondent answers generated and attached to case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> generateCoRespondentAnswers(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(caseOrchestrationService
                .generateCoRespondentAnswers(ccdCallbackRequest, authorizationToken));
            log.info("Co-respondent answer generated. Case id: {}", caseId);
        } catch (WorkflowException exception) {
            log.error("Co-respondent answer generation failed. Case id:  {}", caseId, exception);
            callbackResponseBuilder.errors(asList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    /**
     * Generate given document and attach it to the Divorce case.
     *
     * @deprecated Please don't use this endpoint.
     *      It's not desirable that document generation information be contained in CCD. Instead it should be known by COS.
     *      Also, having values outside of COS makes re-using the code harder and less safe.
     *      Please create an endpoint for your specific need.
     */
    @Deprecated(since = "We had to re-generate documents whose input information was contained in CCD (making reusability unsafe).")
    @PostMapping(path = "/generate-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Generate given document and attach it to the Divorce case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Document has been attached to the case", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> generateDocument(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestParam(value = "templateId") @ApiParam("templateId") String templateId,
        @RequestParam(value = "documentType") @ApiParam("documentType") String documentType,
        @RequestParam(value = "filename") @ApiParam("filename") String filename,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        log.warn("The /generate-document endpoint was called with templateId [{}], documentType [{}] and filename [{}].",
            templateId,
            documentType,
            filename);

        return generateNewDocumentAndAddToCaseData(authorizationToken, ccdCallbackRequest, templateId, documentType, filename);
    }

    @PostMapping(path = "/prepare-to-print-for-pronouncement", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Prepare case data before printing for pronouncement")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Document has been attached to the case", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> prepareToPrintForPronouncement(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return generateNewDocumentAndAddToCaseData(authorizationToken,
            ccdCallbackRequest,
            CASE_LIST_FOR_PRONOUNCEMENT,
            CASE_LIST_FOR_PRONOUNCEMENT_DOCUMENT_TYPE,
            CASE_LIST_FOR_PRONOUNCEMENT_FILE_NAME);
    }

    @PostMapping(path = "/update-bulk-case-hearing-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Prepare case data before updating bulk case hearing details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Document has been attached to the case", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> updateBulkCaseHearingDetails(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        String ccdDocumentType = "coe";
        String filename = CERTIFICATE_OF_ENTITLEMENT_FILENAME_PREFIX;

        return generateNewDocumentAndAddToCaseData(authorizationToken, ccdCallbackRequest, COE, ccdDocumentType, filename);
    }

    @PostMapping(path = "/generate-dn-pronouncement-documents", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Generate the documents for Decree Nisi Pronouncement and attach them to the case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "DN Pronouncement documents have been attached to the case", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> generateDnDocuments(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(caseOrchestrationService.handleDnPronouncementDocumentGeneration(ccdCallbackRequest, authorizationToken));
            log.info("Generated DN documents for Case ID: {}.", caseId);
        } catch (WorkflowException exception) {
            log.error("DN document generation failed for Case ID: {}", caseId, exception);
            callbackResponseBuilder.errors(Collections.singletonList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/da-about-to-be-granted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Generate Decree Absolute Certificate and email notifications to Petitioner and Respondent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "DA Certificate generated and emails sent to Petitioner and Respondent",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> daAboutToBeGranted(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(caseOrchestrationService.handleGrantDACallback(ccdCallbackRequest, authorizationToken));
            log.info("Generated DA documents for case ID: {}.", caseId);
        } catch (WorkflowException exception) {
            log.error("DA document generation failed for case ID: {}", caseId, exception);
            callbackResponseBuilder.errors(singletonList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/handle-post-da-granted")
    @ApiOperation(value = "Callback to run after DA Grant event has finished")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "DA Granted callback processed")})
    public ResponseEntity<CcdCallbackResponse> handleDaGranted(
        @RequestHeader(value = AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(caseOrchestrationService.handleDaGranted(ccdCallbackRequest, authorizationToken));
            log.info("Handled DA granted for case ID: {}.", caseId);
        } catch (WorkflowException exception) {
            log.error("DA granted handling has failed for case ID: {}", caseId, exception);
            callbackResponseBuilder.errors(singletonList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/aos-received")
    @ApiOperation(value = "Trigger notification email to Respondent to confirm they have successfully submitted their AOS")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Notification email to Respondent for AOS Received sent successful"),
        @ApiResponse(code = 401, message = "User Not Authenticated"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> aosReceived(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(caseOrchestrationService.aosReceived(ccdCallbackRequest, authorizationToken));
    }

    @PostMapping(path = "/co-respondent-received")
    @ApiOperation(value = "Trigger notification email to Co-Respondent to confirm they have successfully submitted their version of AOS")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Notification email to Co-Respondent for AOS Received sent successful"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> corespReceived(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(caseOrchestrationService.sendCoRespReceivedNotificationEmail(ccdCallbackRequest));
    }

    @PostMapping(path = "/aos-solicitor-nominated", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Handles actions that need to happen once a Respondent nominates a Solicitor.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Respondent successfully nominated their Solicitor",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> aosSolicitorNominated(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody @ApiParam("CaseData")
            CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.debug("Processing AOS Solicitor nominated callback. Case ID: {}", caseId);

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(
                caseOrchestrationService.processAosSolicitorNominated(ccdCallbackRequest, authToken));
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format("Failed processing AOS solicitor callback. Case ID:  %s", caseId), exception);
            callbackResponseBuilder.errors(Collections.singletonList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/calculate-separation-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to calculate CCD separation fields based on provided separation dates.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error, message is attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> calculateSeparationFields(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            Map<String, Object> response = caseOrchestrationService.processSeparationFields(ccdCallbackRequest);

            if (response != null && response.containsKey(VALIDATION_ERROR_KEY)) {
                callbackResponseBuilder.errors(singletonList((String) response.get(VALIDATION_ERROR_KEY)));
            } else {
                callbackResponseBuilder.data(response);
            }
        } catch (WorkflowException exception) {
            log.error("Failed processing calculateSeparationFields callback", exception);
            callbackResponseBuilder.errors(Collections.singletonList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/dn-about-to-be-granted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Handles CCD case data just before Decree Nisi is granted")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error, message is attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> dnAboutToBeGranted(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();
        try {
            callbackResponseBuilder.data(caseOrchestrationService.processCaseBeforeDecreeNisiIsGranted(ccdCallbackRequest, authToken));
            log.info("Processed case successfully for case ID: {}", caseId);
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format(FAILED_TO_EXECUTE_SERVICE_ERROR, caseId), exception);
            callbackResponseBuilder.errors(asList(exception.getMessage()));
        } catch (Exception exception) {
            log.error(format(FAILED_TO_EXECUTE_SERVICE_ERROR, caseId), exception);
            callbackResponseBuilder.errors(asList(GENERIC_ERROR_MESSAGE));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/dn-about-to-be-granted-state", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Handles case data state just before Decree Nisi is granted")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error, message is attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> decreeNisiDecisionState(
        @RequestBody @ApiParam("CaseData")
            CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();
        try {
            callbackResponseBuilder.data(caseOrchestrationService.decreeNisiDecisionState(ccdCallbackRequest));
        } catch (WorkflowException exception) {
            log.error(format(FAILED_TO_EXECUTE_SERVICE_ERROR, caseId), exception);
            callbackResponseBuilder.errors(asList(exception.getMessage()));
        } catch (Exception exception) {
            log.error(format(FAILED_TO_EXECUTE_SERVICE_ERROR, caseId), exception);
            callbackResponseBuilder.errors(asList(GENERIC_ERROR_MESSAGE));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/solicitor-link-case")
    @ApiOperation(value = "Authorize the Respondent's Solicitor to the case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Respondent Solicitor Authenticated"),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Not authorised"),
        @ApiResponse(code = 404, message = "Case not found")})
    public ResponseEntity<CcdCallbackResponse> solicitorLinkCase(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.debug("Processing solicitor link case callback for case ID: {}", caseId);

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(
                caseOrchestrationService.processAosSolicitorLinkCase(ccdCallbackRequest, authorizationToken));
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format("Failed solicitor link case callback. Case ID:  %s", caseId),
                exception);
            callbackResponseBuilder.errors(Collections.singletonList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    /**
     * This solution will be removed from CCD, see https://tools.hmcts.net/jira/browse/RDM-6970 for more details
     */
    @PostMapping(path = "/clean-state")
    @ApiOperation(value = "Clear state from CCD Case Data")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully clearer State from case data"),
        @ApiResponse(code = 401, message = "Not authorised"),
        @ApiResponse(code = 404, message = "Case not found")})
    public ResponseEntity<CcdCallbackResponse> clearStateCallback(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        callbackResponseBuilder.data(caseOrchestrationService.cleanStateCallback(ccdCallbackRequest, authorizationToken));

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.debug("Cleared case state from CCD for case ID: {}", caseId);

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/dn-decision-made")
    @ApiOperation(value = "Perform post Decree Nisi make decision event actions")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed"),
        @ApiResponse(code = 401, message = "Not authorised"),
        @ApiResponse(code = 404, message = "Case not found")})
    public ResponseEntity<CcdCallbackResponse> dnDecisionMadeCallback(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        if (CaseDataUtils.isWelshLADecisionTranslationRequired(ccdCallbackRequest.getCaseDetails())) {
            callbackResponseBuilder.data(ccdCallbackRequest.getCaseDetails().getCaseData());
            return ResponseEntity.ok(callbackResponseBuilder.build());
        }
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.info("DN Decision made - Notifying refusal order. Case ID: {}", caseId);
        caseOrchestrationService.notifyForRefusalOrder(ccdCallbackRequest);
        log.info("DN Decision made - cleaning state for case ID: {}", caseId);
        callbackResponseBuilder.data(caseOrchestrationService.cleanStateCallback(ccdCallbackRequest, authorizationToken));

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/process-applicant-da-eligibility")
    @ApiOperation(value = "Callback to be called when case is about to become eligible for DA (for Petitioner).")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed"),
        @ApiResponse(code = 401, message = "Not authorised"),
        @ApiResponse(code = 404, message = "Case not found")})
    public ResponseEntity<CcdCallbackResponse> processApplicantDecreeAbsoluteEligibility(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {
        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        try {
            callbackResponseBuilder.data(caseOrchestrationService.processApplicantDecreeAbsoluteEligibility(ccdCallbackRequest));
            log.info("Processed processApplicantDecreeAbsoluteEligibility callback request for case ID: {}", caseId);
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format(FAILED_TO_EXECUTE_SERVICE_ERROR, caseId), exception);
            callbackResponseBuilder.errors(asList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/handle-post-make-case-eligible-for-da-submitted")
    @ApiOperation(value = "Callback to run after 'Make Case Eligible For DA' event has finished")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed")})
    public ResponseEntity<CcdCallbackResponse> handleMakeCaseEligibleForDaSubmitted(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        Map<String, Object> caseData = caseOrchestrationService.handleMakeCaseEligibleForDaSubmitted(ccdCallbackRequest);
        log.info("Processed handleMakeCaseEligibleForDaSubmitted successfully for case id {}", ccdCallbackRequest.getCaseDetails().getCaseId());

        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(caseData)
            .build());
    }

    @PostMapping(path = "/remove-bulk-link")
    @ApiOperation(value = "Set bulk link to null")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed")})
    public ResponseEntity<CcdCallbackResponse> removeBulkLinkFromCase(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        callbackResponseBuilder.data(caseOrchestrationService.removeBulkLink(ccdCallbackRequest));
        log.info("Removed bulk link for case ID: {}", caseId);

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/issue-aos-pack-offline/parties/{party}")
    @ApiOperation(value = "Issue AOS pack offline to Respondent or Co-Respondent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed")})
    public ResponseEntity<CcdCallbackResponse> issueAosPackOffline(
        @RequestHeader(name = AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM", required = true) String authToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest,
        @PathVariable("party")
        @ApiParam(value = "Party in divorce (respondent or co-respondent)",
            allowableValues = "respondent, co-respondent") DivorceParty party) {
        CcdCallbackResponse response;
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        try {
            response = CcdCallbackResponse.builder()
                .data(aosService.issueAosPackOffline(authToken, caseDetails, party))
                .build();
            log.info("Issued offline AOS pack for case ID: {}", caseDetails.getCaseId());
        } catch (CaseOrchestrationServiceException exception) {
            response = CcdCallbackResponse.builder()
                .errors(singletonList(exception.getMessage()))
                .build();
            log.error("Error issuing offline AOS pack for case ID: {}", caseDetails.getCaseId());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/processAosOfflineAnswers/parties/{party}")
    @ApiOperation(value = "Process offline AOS Answers for Respondent or Co-Respondent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully processed offline AOS Answers")})
    public ResponseEntity<CcdCallbackResponse> processAosPackOfflineAnswers(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest,
        @PathVariable("party")
        @ApiParam(value = "Party in divorce (respondent or co-respondent)",
            allowableValues = "respondent, co-respondent") DivorceParty party) {
        CcdCallbackResponse.CcdCallbackResponseBuilder responseBuilder = CcdCallbackResponse.builder();
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        try {
            responseBuilder.data(aosService.processAosPackOfflineAnswers(authorizationToken, caseDetails, party));
            log.info("Processed AOS offline answers for {} of case {}", party.getDescription(), caseDetails.getCaseId());
        } catch (CaseOrchestrationServiceException exception) {
            log.info("Error processing AOS offline answers for {} of case {}", party.getDescription(), caseDetails.getCaseId());
            log.error(exception.getMessage(), exception);
            responseBuilder.errors(singletonList(exception.getMessage()));
        }

        return ResponseEntity.ok(responseBuilder.build());
    }

    @PostMapping(path = "/listing/remove-bulk-link")
    @ApiOperation(value = "Callback to unlink case from bulk case listed")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case unlinked from bulk case listed")})
    public ResponseEntity<CcdCallbackResponse> removeBulkLinkFromCaseListed(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        callbackResponseBuilder.data(caseOrchestrationService.removeBulkListed(ccdCallbackRequest));
        log.info("Remove bulk listed for case ID: {}", caseId);

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/remove-dn-outcome-case-flag")
    @ApiOperation(value = "Callback to remove the DnOutcomeCase flag")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully removed the DnOutcomeCase flag")})
    public ResponseEntity<CcdCallbackResponse> removeDnOutcomeCaseFlag(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(caseOrchestrationService.removeDnOutcomeCaseFlag(ccdCallbackRequest))
                .build());
    }

    @PostMapping(path = "/remove-la-make-decision-fields")
    @ApiOperation(value = "Callback to remove the fields set by the Legal Advisor when they make a decision.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.")})
    public ResponseEntity<CcdCallbackResponse> removeLegalAdvisorMakeDecisionFields(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(caseOrchestrationService.removeLegalAdvisorMakeDecisionFields(ccdCallbackRequest))
                .build());
    }

    @PostMapping(path = "/pronouncement/cancel")
    @ApiOperation(value = "Callback to cancel DN pronouncement.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.")})
    public ResponseEntity<CcdCallbackResponse> removeDNGrantedDocuments(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        CcdCallbackResponse.CcdCallbackResponseBuilder response = CcdCallbackResponse.builder();

        try {
            response.data(caseOrchestrationService.removeDNGrantedDocuments(ccdCallbackRequest))
                .build();
            log.info("Delete DN granted documents for case ID: {}", ccdCallbackRequest.getCaseDetails().getCaseId());
        } catch (WorkflowException exception) {
            response.errors(singletonList(exception.getMessage()))
                .build();
            log.error("Failed to delete DN granted documents for case ID: {}", ccdCallbackRequest.getCaseDetails().getCaseId(), exception);
        }
        return ResponseEntity.ok(response.build());
    }

    @PostMapping(path = "/amend-application")
    @ApiOperation(value = "Trigger notification email to Petitioner that they are able to amend their application.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Amend application notification callback processed.")})
    public ResponseEntity<CcdCallbackResponse> amendApplication(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(caseOrchestrationService.sendAmendApplicationEmail(ccdCallbackRequest))
                .build());
    }

    @PostMapping(path = "/welsh-event-intercept")
    @ApiOperation(value = "Callback to set next event upon receival of translation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.")})
    public ResponseEntity<CcdCallbackResponse> welshContinue(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(caseOrchestrationService.welshContinue(ccdCallbackRequest))
                .build());
    }

    @PostMapping(path = "/welshSetPreviousState")
    @ApiOperation(value = "Callback to set next event based on previous state.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.")})
    public ResponseEntity<CcdCallbackResponse> welshSetPreviousState(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        return ResponseEntity.ok(caseOrchestrationService.welshSetPreviousState(ccdCallbackRequest));
    }

    @PostMapping(path = "/welsh-state-intercept", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to set next state of current Event based on previous state")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> welshContinueIntercept(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(caseOrchestrationService.welshContinueIntercept(ccdCallbackRequest, authorizationToken));
    }

    @PostMapping(path = "/received-service-added-date", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to set ReceivedServiceAddedDate field to 'now'")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> receivedServiceAddedDate(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws ServiceJourneyServiceException {
        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(serviceJourneyService.receivedServiceAddedDate(ccdCallbackRequest))
                .build()
        );
    }

    @PostMapping(path = "/make-service-decision", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to set state on service decision")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> makeServiceDecision(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws ServiceJourneyServiceException {

        return ResponseEntity.ok(
            serviceJourneyService
                .makeServiceDecision(ccdCallbackRequest.getCaseDetails(), authorizationToken)
        );
    }

    @PostMapping(path = "/service-decision-made/draft", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to set document for service after decision")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> draftDocumentsForServiceDecision(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            serviceJourneyService
                .serviceDecisionRefusal(ccdCallbackRequest.getCaseDetails(), authorizationToken)
        );
    }

    @PostMapping(path = "/service-decision-made/final", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to set document for service after decision")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> serviceDecisionMade(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            serviceJourneyService.serviceDecisionMade(ccdCallbackRequest.getCaseDetails(), authorizationToken)
        );
    }

    @PostMapping(path = "/create-general-order/draft", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to generate draft general order document")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> generateDraftOfGeneralOrder(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(generalOrderService
                    .generateGeneralOrderDraft(ccdCallbackRequest.getCaseDetails(), authorizationToken)
                    .getCaseData()
                ).build()
        );
    }

    @PostMapping(path = "/create-general-order/final", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to generate general order document")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> generateGeneralOrder(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(generalOrderService
                    .generateGeneralOrder(ccdCallbackRequest.getCaseDetails(), authorizationToken)
                    .getCaseData()
                ).build()
        );
    }

    @PostMapping(path = "/set-up-confirm-service-payment")
    @ApiOperation(value = "Return service payment fee.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Service payment callback")})
    public ResponseEntity<CcdCallbackResponse> setupConfirmServicePaymentEvent(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(serviceJourneyService.setupConfirmServicePaymentEvent(ccdCallbackRequest.getCaseDetails()))
                .build());
    }

    @PostMapping(path = "/confirm-service-payment")
    @ApiOperation(value = "Returns updated case data with payment reference collection")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Service payment confirmation callback")})
    public ResponseEntity<CcdCallbackResponse> confirmServicePaymentEvent(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(serviceJourneyService.confirmServicePaymentEvent(ccdCallbackRequest.getCaseDetails()))
                .build());
    }

    @PostMapping(path = "/set-up-order-summary/without-notice-fee")
    @ApiOperation(value = "Return service payment fee. Starting from state AwaitingGeneralReferralPayment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Service payment callback")})
    public ResponseEntity<CcdCallbackResponse> setupGeneralReferralPaymentEvent(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(generalReferralService.setupGeneralReferralPaymentEvent(ccdCallbackRequest.getCaseDetails()))
                .build());
    }

    @PostMapping(path = "/general-referral-payment")
    @ApiOperation(value = "Returns updated case data with payment reference collection")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "General referral payment confirmation callback")})
    public ResponseEntity<CcdCallbackResponse> generalReferralPaymentEvent(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(generalReferralService.generalReferralPaymentEvent(ccdCallbackRequest.getCaseDetails()))
                .build());
    }

    @PostMapping(path = "/prepare-aos-not-received-for-submission")
    @ApiOperation(value = "Prepare event for submission")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    public ResponseEntity<CcdCallbackResponse> prepareAosNotReceivedForSubmission(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(aosService.prepareAosNotReceivedEventForSubmission(authorizationToken, ccdCallbackRequest.getCaseDetails()))
                .build());
    }

    @PostMapping(path = "/create-general-email")
    @ApiOperation(value = "Prepare event to send general email")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Create general email notification callback processed.")})
    public ResponseEntity<CcdCallbackResponse> createGeneralEmail(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(generalEmailService.createGeneralEmail(ccdCallbackRequest.getCaseDetails()))
                .build());
    }

    @PostMapping(path = "/general-referral", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback for general referral")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> generalReferral(@RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest)
        throws CaseOrchestrationServiceException {

        CcdCallbackResponse response = generalReferralService.receiveReferral(ccdCallbackRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/general-consideration", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback for general consideration")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> generalConsideration(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest)
        throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(generalReferralService.generalConsideration(ccdCallbackRequest.getCaseDetails()))
                .build()
        );
    }

    @PostMapping(path = "/confirm-alternative-service", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback for general consideration")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> confirmAlternativeService(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest)
        throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(alternativeServiceService.confirmAlternativeService(ccdCallbackRequest.getCaseDetails()).getCaseData())
                .build()
        );
    }

    @PostMapping(path = "/return-to-state-before-general-referral", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to set a case back to the state it had before triggering a General Referral")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> returnToStateBeforeGeneralReferral(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest)
        throws CaseOrchestrationServiceException {

        CcdCallbackResponse response = generalReferralService.returnToStateBeforeGeneralReferral(ccdCallbackRequest.getCaseDetails());
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/confirm-process-server-service", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback for confirm process server service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback for confirm process server service has been processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> confirmProcessServerService(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest)
        throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(alternativeServiceService.confirmProcessServerService(ccdCallbackRequest.getCaseDetails()).getCaseData())
                .build()
        );
    }

    @PostMapping(path = "/aos-not-received-for-process-server", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback for aos not received for process server")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> aosNotReceivedForProcessServer(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest)
        throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(alternativeServiceService.aosNotReceivedForProcessServer(ccdCallbackRequest.getCaseDetails()).getCaseData())
                .build()
        );
    }

    @PostMapping(path = "/alternative-service-confirmed", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback for confirm alternative service (submitted)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> alternativeServiceConfirmed(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest)
        throws CaseOrchestrationServiceException {

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(alternativeServiceService.alternativeServiceConfirmed(ccdCallbackRequest.getCaseDetails()).getCaseData())
                .build()
        );
    }

    @PostMapping(path = "/update-court-order-documents", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback for generating new versions of existing court order documents")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> updateCourtOrderDocuments(
        @RequestHeader(value = AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws CaseOrchestrationServiceException {

        Map<String, Object> returnedPayload = courtOrderDocumentsUpdateService.updateExistingCourtOrderDocuments(
            authorizationToken,
            ccdCallbackRequest.getCaseDetails()
        );

        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(returnedPayload)
                .build()
        );
    }

    @ExceptionHandler(CaseOrchestrationServiceException.class)
    ResponseEntity<CcdCallbackResponse> handleCaseOrchestrationServiceExceptionForCcdCallback(CaseOrchestrationServiceException exception) {
        log.error(exception.getIdentifiableMessage(), exception);

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse.builder()
            .errors(singletonList(exception.getMessage()))
            .build();

        return ResponseEntity.ok(ccdCallbackResponse);
    }

    private List<String> getErrors(Map<String, Object> response) {
        ValidationResponse validationResponse = (ValidationResponse) response.get(VALIDATION_ERROR_KEY);
        return validationResponse.getErrors();
    }

    private ResponseEntity<CcdCallbackResponse> generateNewDocumentAndAddToCaseData(String authorizationToken,
                                                                                    CcdCallbackRequest ccdCallbackRequest,
                                                                                    String templateId,
                                                                                    String ccdDocumentType,
                                                                                    String filename) throws CaseOrchestrationServiceException {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        Map<String, Object> payloadToReturn = caseOrchestrationService.handleDocumentGenerationCallback(
            ccdCallbackRequest, authorizationToken, templateId, ccdDocumentType, filename
        );
        callbackResponseBuilder.data(payloadToReturn);
        log.info("Generating document {} for case {}.", ccdDocumentType, caseId);

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    private ResponseEntity<CcdCallbackResponse> generateNewDocumentAndAddToCaseData(String authorizationToken,
                                                                                    CcdCallbackRequest ccdCallbackRequest,
                                                                                    DocumentType documentType,
                                                                                    String ccdDocumentType,
                                                                                    String filename) throws CaseOrchestrationServiceException {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        Map<String, Object> payloadToReturn = caseOrchestrationService.handleDocumentGenerationCallback(
            ccdCallbackRequest, authorizationToken, documentType, ccdDocumentType, filename
        );
        callbackResponseBuilder.data(payloadToReturn);
        log.info("Generating document {} for case {}.", ccdDocumentType, caseId);

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

}