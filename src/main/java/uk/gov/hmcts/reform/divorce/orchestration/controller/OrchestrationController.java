package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_PRINT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CHECK_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_VALIDATION_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

@Slf4j
@RestController
public class OrchestrationController {

    @Autowired
    private CaseOrchestrationService orchestrationService;

    @PostMapping(path = "/petition-issued",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles Issue callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case",
                    response = CcdCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
            })
    public ResponseEntity<CcdCallbackResponse> petitionIssuedCallback(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @RequestBody @ApiParam("CaseData") CreateEvent caseDetailsRequest) throws WorkflowException {
        Map<String, Object> response = orchestrationService.ccdCallbackHandler(caseDetailsRequest, authorizationToken);

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

    @PostMapping(path = "/bulk-print",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles bulk print callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
            + "attached to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public ResponseEntity<CcdCallbackResponse> bulkPrint(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @RequestBody @ApiParam("CaseData") CreateEvent caseDetailsRequest) throws WorkflowException {

        Map<String, Object> response = orchestrationService.ccdCallbackBulkPrintHandler(caseDetailsRequest,
            authorizationToken);

        if (response != null && response.containsKey(BULK_PRINT_ERROR_KEY)) {
            return ResponseEntity.ok(
                CcdCallbackResponse.builder()
                    .data(ImmutableMap.of())
                    .warnings(ImmutableList.of())
                    .errors(Collections.singletonList("Failed to bulk print documents"))
                    .build());
        }
        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(response)
                .errors(Collections.emptyList())
                .warnings(Collections.emptyList())
                .build());
    }

    @PostMapping(path = "/submit",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles submit called from petition frontend")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Submit was successful and case was created in CCD",
                    response = CaseResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<CaseResponse> submit(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) throws WorkflowException {

        Map<String, Object> response = orchestrationService.submit(payload, authorizationToken);

        if (response.containsKey(VALIDATION_ERROR_KEY)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(
                CaseResponse.builder()
                        .caseId(response.get(ID).toString())
                        .status(SUCCESS_STATUS)
                        .build());
    }

    @PostMapping(path = "/updateCase/{caseId}",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles update called from petition frontend")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Update was successful and case was updated in CCD",
                    response = CaseResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<CaseResponse> update(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @PathVariable String caseId,
            @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) throws WorkflowException {

        return ResponseEntity.ok(
                CaseResponse.builder()
                        .caseId(orchestrationService.update(payload, authorizationToken, caseId).get(ID).toString())
                        .status(SUCCESS_STATUS)
                        .build());
    }

    @GetMapping(path = "/draftsapi/version/1", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieves a divorce case draft")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A draft exists. The draft content is in the response body"),
            @ApiResponse(code = 404, message = "Draft does not exist")})
    public ResponseEntity<Map<String, Object>> retrieveDraft(
            @RequestHeader("Authorization") @ApiParam(value = "JWT authorisation token issued by IDAM", required = true)
            final String authorizationToken) throws WorkflowException {

        Map<String, Object>    response = orchestrationService.getDraft(authorizationToken);
        if (MapUtils.isEmpty(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/draftsapi/version/1", consumes = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Saves or updates a draft to draft store")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Draft saved")})
    public ResponseEntity<Map<String, Object>> saveDraft(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            @ApiParam(value = "JWT authorisation token issued by IDAM", required = true)
                final String authorizationToken,
            @RequestBody
            @ApiParam(value = "The case draft", required = true)
            @NotNull Map<String, Object> payload,
            @RequestParam(value = "notificationEmail", required = false)
            @ApiParam(value = "The email address that will receive the notification that the draft has been saved")
            @Email final String notificationEmail) throws WorkflowException {

        return ResponseEntity.ok(orchestrationService.saveDraft(payload, authorizationToken, notificationEmail));
    }

    @DeleteMapping(path = "/draftsapi/version/1")
    @ApiOperation(value = "Deletes a divorce case draft")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The divorce draft has been deleted successfully")})
    public ResponseEntity<Map<String, Object>> deleteDraft(@RequestHeader("Authorization")
                                            @ApiParam(value = "JWT authorisation token issued by IDAM",
                                                    required = true) final String authorizationToken)
            throws WorkflowException {

        return ResponseEntity.ok(orchestrationService.deleteDraft(authorizationToken));
    }

    @GetMapping(path = "/retrieve-aos-case", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Provides case details to front end")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case details fetched successfully",
            response = CaseDataResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")
        })
    public ResponseEntity<CaseDataResponse> retrieveAosCase(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationToken,
        @RequestParam @ApiParam(CHECK_CCD) boolean checkCcd) throws WorkflowException {

        return ResponseEntity.ok(orchestrationService.retrieveAosCase(checkCcd,
            authorizationToken));
    }

    @PostMapping(path = "/authenticate-respondent")
    @ApiOperation(value = "Authenticates the respondent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Respondent Authenticated"),
            @ApiResponse(code = 401, message = "User Not Authenticated"),
            @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<Void> authenticateRespondent(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationToken) {
        Boolean authenticateRespondent = null;

        try {
            authenticateRespondent = orchestrationService.authenticateRespondent(authorizationToken);
        } catch (WorkflowException e) {
            log.error(e.getMessage(), e);
        }

        if (authenticateRespondent != null && authenticateRespondent) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping(path = "/link-respondent/{caseId}/{pin}")
    @ApiOperation(value = "Authorize the respondent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Respondent Authenticated"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 401, message = "User Not Authenticated"),
        @ApiResponse(code = 404, message = "Case Not found")
        })
    public ResponseEntity<UserDetails> linkRespondent(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationToken,
        @PathVariable("caseId") @ApiParam("Unique identifier of the session that was submitted to CCD") String caseId,
        @PathVariable("pin") @ApiParam(value = "Pin", required = true) String pin) throws WorkflowException {

        UserDetails linkRespondent = orchestrationService.linkRespondent(authorizationToken, caseId, pin);

        if (linkRespondent != null) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping(path = "/petition-submitted",
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Generate/dispatch a notification email to the petitioner when the application is submitted")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "An email notification has been generated and dispatched",
                    response = CcdCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<CcdCallbackResponse> petitionSubmitted(
            @RequestHeader(value = "Authorization", required = false) String authorizationToken,
            @RequestBody @ApiParam("CaseData") CreateEvent caseDetailsRequest) throws WorkflowException {

        orchestrationService.sendSubmissionNotificationEmail(caseDetailsRequest);

        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(caseDetailsRequest.getCaseDetails().getCaseData())
            .build());
    }

    @PostMapping(path = "/petition-issue-fees",
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Return a order summary for petition issue")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Petition issue fee amount is send to CCD as callback response",
                    response = CcdCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<CcdCallbackResponse> getPetitionIssueFees(
            @RequestBody @ApiParam("CaseData") CreateEvent caseDetailsRequest) throws WorkflowException {
        return ResponseEntity.ok(CcdCallbackResponse.builder()
                .data(orchestrationService.setOrderSummary(caseDetailsRequest))
                .build()
        );
    }

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/process-pba-payment", consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Solicitor pay callback")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback to receive payment from the solicitor",
                    response = CcdCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<CcdCallbackResponse> processPbaPayment(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @RequestBody @ApiParam("CaseData") CreateEvent caseDetailsRequest) throws WorkflowException {
        Map<String, Object> response = orchestrationService.processPbaPayment(caseDetailsRequest, authorizationToken);

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
            @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<CcdCallbackResponse> solicitorCreate(
            @RequestBody @ApiParam("CaseData") CreateEvent caseDetailsRequest) throws WorkflowException {
        return ResponseEntity.ok(CcdCallbackResponse.builder()
                .data(orchestrationService.solicitorCreate(caseDetailsRequest))
                .build());
    }

    @PostMapping(path = "/aos-received")
    @ApiOperation(value = "Respondent confirmation notification ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Notification sent successful"),
            @ApiResponse(code = 401, message = "User Not Authenticated"),
            @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> aosReceived(
            @RequestHeader("Authorization")
            @ApiParam(value = "JWT authorisation token issued by IDAM",
                    required = true) final String authorizationToken,
            @RequestBody @ApiParam("CaseData") CreateEvent caseDetailsRequest) throws WorkflowException {
        return ResponseEntity.ok(orchestrationService.aosReceived(caseDetailsRequest, authorizationToken));
    }

    @PostMapping(path = "/submit-aos/{caseId}",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles AOS submission")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Update was successful and case was updated in CCD",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")
        })
    public ResponseEntity<Map<String, Object>> submitAos(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @PathVariable String caseId,
        @RequestBody @ApiParam("Complete Divorce Session / partial Aos data ") Map<String, Object> payload)
        throws WorkflowException {

        return ResponseEntity.ok(
            orchestrationService.submitAosCase(payload, authorizationToken, caseId));
    }

    private List<String> getErrors(Map<String, Object> response) {
        ValidationResponse validationResponse = (ValidationResponse) response.get(VALIDATION_ERROR_KEY);
        return validationResponse.getErrors();
    }
}
