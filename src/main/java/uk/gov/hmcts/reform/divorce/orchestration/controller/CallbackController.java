package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_PRINT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_ANSWERS_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_ANSWERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.GENERATE_AOS_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ANSWERS_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_VALIDATION_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

/**
 * Controller class for callback endpoints.
 */
@RestController
@Slf4j
public class CallbackController {

    private static final String GENERIC_ERROR_MESSAGE = "An error happened when processing this request.";

    @Autowired
    private CaseOrchestrationService caseOrchestrationService;


    @PostMapping(path = "/request-clarification-petitioner")
    @ApiOperation(value = "Request clarification from petitioner via notification ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "clarification request sent successful"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> requestClarificationFromPetitioner(
        @RequestBody @ApiParam("CaseData") final CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        caseOrchestrationService.sendPetitionerClarificationRequestNotification(ccdCallbackRequest);
        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build());
    }

    @PostMapping(path = "/dn-submitted")
    @ApiOperation(value = "Decree nisi submitted confirmation notification ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Notification sent successful"),
        @ApiResponse(code = 401, message = "User Not Authenticated"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> dnSubmitted(
        @RequestHeader("Authorization")
        @ApiParam(value = "Authorisation token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(caseOrchestrationService.dnSubmitted(ccdCallbackRequest, authorizationToken));
    }

    @PostMapping(path = "/dn-pronounced",
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Generate/dispatch a notification email to the petitioner and respondent when the Decree Nisi has been pronounced")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "An email notification has been generated and dispatched",
                    response = CcdCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> dnPronounced(
            @RequestHeader(value = "Authorization", required = false) String authorizationToken,
            @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        caseOrchestrationService.sendDnPronouncedNotificationEmail(ccdCallbackRequest);
        return ResponseEntity.ok(CcdCallbackResponse.builder()
                .data(ccdCallbackRequest.getCaseDetails().getCaseData())
                .build());
    }

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/process-pba-payment", consumes = MediaType.APPLICATION_JSON,
        produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Solicitor pay callback")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback to receive payment from the solicitor",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> processPbaPayment(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        Map<String, Object> response = caseOrchestrationService.processPbaPayment(ccdCallbackRequest, authorizationToken);

        if (response != null && response.containsKey(SOLICITOR_VALIDATION_ERROR_KEY)) {
            return ResponseEntity.ok(
                CcdCallbackResponse.builder()
                    .errors((List<String>) response.get(SOLICITOR_VALIDATION_ERROR_KEY))
                    .build());
        }

        return ResponseEntity.ok(CcdCallbackResponse.builder().data(response).build());
    }

    @PostMapping(path = "/solicitor-create", consumes = MediaType.APPLICATION_JSON,
        produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Solicitor pay callback")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback to populate missing requirement fields when "
            + "creating solicitor cases.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> solicitorCreate(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(caseOrchestrationService.solicitorCreate(ccdCallbackRequest))
            .build());
    }

    @PostMapping(path = "/aos-submitted",
        consumes = MediaType.APPLICATION_JSON,
        produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Generate/dispatch a notification email to the respondent when their AOS is submitted")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "An email notification has been generated and dispatched",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> respondentAOSSubmitted(
        @RequestHeader(value = "Authorization", required = false) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.info("/aos-submitted endpoint called for caseId {}", caseId);
        Map<String, Object> returnedCaseData;

        try {
            returnedCaseData = caseOrchestrationService.sendRespondentSubmissionNotificationEmail(ccdCallbackRequest);
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

    @PostMapping(path = "/petition-updated",
        consumes = MediaType.APPLICATION_JSON,
        produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Generate/dispatch a notification email to the petitioner when the application is updated")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "An email notification has been generated and dispatched",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> petitionUpdated(
        @RequestHeader(value = "Authorization", required = false) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        caseOrchestrationService.sendPetitionerGenericUpdateNotificationEmail(ccdCallbackRequest);
        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build());
    }

    @PostMapping(path = "/petition-submitted",
        consumes = MediaType.APPLICATION_JSON,
        produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Generate/dispatch a notification email to the petitioner when the application is submitted")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "An email notification has been generated and dispatched",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> petitionSubmitted(
        @RequestHeader(value = "Authorization", required = false) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        caseOrchestrationService.sendPetitionerSubmissionNotificationEmail(ccdCallbackRequest);

        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build());
    }

    @PostMapping(path = "/bulk-print",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles bulk print callback from CCD")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Callback was processed "
        + "successfully or in case of an error message is "
        + "attached to the case",
        response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> bulkPrint(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        Map<String, Object> response = caseOrchestrationService.ccdCallbackBulkPrintHandler(ccdCallbackRequest,
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

    @PostMapping(path = "/petition-issued",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles Issue event callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is "
            + "attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> petitionIssuedCallback(
        @RequestHeader(value = "Authorization") String authorizationToken,
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

    @PostMapping(path = "/case-linked-for-hearing",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles actions that need to happen once the case has been linked for hearing (been given a hearing date).")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error, message is "
            + "attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> caseLinkedForHearing(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.debug("Processing case linked for hearing. Case id: {}", caseId);

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(
                caseOrchestrationService.processCaseLinkedForHearingEvent(ccdCallbackRequest));
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format("Failed to execute service to process case linked for hearing. Case id:  %s", caseId),
                exception);
            callbackResponseBuilder.errors(ImmutableList.of(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/co-respondent-answered",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Set co-respondent answer received fields on divorce case.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error, message is "
            + "attached to the case",
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

    @PostMapping(path = "/sol-dn-review-petition",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Populates fields for solicitor DN journey")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> solDnReviewPetition(@RequestBody CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.debug("Processing solicitor DN review petition for Case ID: {}", caseId);

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
            log.error(format("Failed to process solicitor DN review petition for Case ID: %s", caseId), exception);
            callbackResponseBuilder.errors(ImmutableList.of(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/sol-dn-resp-answers-doc",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Populates Respondent Answers doc for solicitor DN journey")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> solDnRespAnswersDoc(@RequestBody CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.debug("Processing solicitor DN Respondent Answers doc for Case ID: {}", caseId);

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(
                caseOrchestrationService.processSolDnDoc(
                    ccdCallbackRequest,
                    DOCUMENT_TYPE_RESPONDENT_ANSWERS,
                    RESP_ANSWERS_LINK
                )
            );
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format("Failed to process solicitor DN review petition for Case ID: %s", caseId), exception);
            callbackResponseBuilder.errors(ImmutableList.of(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/sol-dn-co-resp-answers-doc",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Populates Co-Respondent Answers doc for solicitor DN journey")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = CcdCallbackResponse.class),
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
            log.error(format("Failed to process solicitor DN review petition for Case ID: %s", caseId), exception);
            callbackResponseBuilder.errors(ImmutableList.of(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/co-respondent-generate-answers",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Generates the Co-Respondent Answers PDF Document for the case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Co-Respondent answers generated and attached to case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> generateCoRespondentAnswers(
        @RequestHeader(value = "Authorization") String authorizationToken,
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

    @PostMapping(path = "/generate-document", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Generates the document and attaches it to the case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Document has been attached to the case", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> generateDocument(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @RequestParam(value = "templateId") @ApiParam("templateId") String templateId,
        @RequestParam(value = "documentType") @ApiParam("documentType") String documentType,
        @RequestParam(value = "filename") @ApiParam("filename") String filename,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(caseOrchestrationService
                .handleDocumentGenerationCallback(ccdCallbackRequest, authorizationToken, templateId, documentType, filename));
            log.info("Generating document {} for case {}. Case id: {}", documentType, caseId);
        } catch (WorkflowException exception) {
            log.error("Document generation failed. Case id:  {}", caseId, exception);
            callbackResponseBuilder.errors(asList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/aos-solicitor-nominated",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles actions that need to happen once a respondent nominates a solicitor.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error, message is "
            + "attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> aosSolicitorNominated(
        @RequestBody @ApiParam("CaseData")
            CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.debug("Processing AOS solicitor nominated callback. Case ID: {}", caseId);

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(
                caseOrchestrationService.processAosSolicitorNominated(ccdCallbackRequest));
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format("Failed processing AOS solicitor callback. Case ID:  %s", caseId),
                exception);
            callbackResponseBuilder.errors(Collections.singletonList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/calculate-separation-fields",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Callback to calculate ccd separation fields based on provided separation dates.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error, message is "
            + "attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> calculateSeparationFields(
        @RequestBody @ApiParam("CaseData")
            CcdCallbackRequest ccdCallbackRequest) {

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            Map<String, Object> response = caseOrchestrationService.processSeparationFields(ccdCallbackRequest);

            if (response != null && response.containsKey(VALIDATION_ERROR_KEY)) {
                callbackResponseBuilder.errors(singletonList((String)response.get(VALIDATION_ERROR_KEY)));
            } else {
                callbackResponseBuilder.data(response);
            }
        } catch (WorkflowException exception) {
            log.error("Failed processing calculateSeparationFields callback", exception);
            callbackResponseBuilder.errors(Collections.singletonList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/dn-about-to-be-granted",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles CCD case data just before Decree Nisi is granted")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error, message is "
            + "attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> dnAboutToBeGranted(
        @RequestBody @ApiParam("CaseData")
            CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();
        try {
            callbackResponseBuilder.data(caseOrchestrationService.processCaseBeforeDecreeNisiIsGranted(ccdCallbackRequest));
            log.info("Processed case successfully. Case id: {}", caseId);
        } catch (CaseOrchestrationServiceException exception) {
            log.error(format("Failed to execute service. Case id:  %s", caseId), exception);
            callbackResponseBuilder.errors(asList(exception.getMessage()));
        } catch (Exception exception) {
            log.error(format("Failed to execute service. Case id:  %s", caseId), exception);
            callbackResponseBuilder.errors(asList(GENERIC_ERROR_MESSAGE));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    private List<String> getErrors(Map<String, Object> response) {
        ValidationResponse validationResponse = (ValidationResponse) response.get(VALIDATION_ERROR_KEY);
        return validationResponse.getErrors();
    }
}
