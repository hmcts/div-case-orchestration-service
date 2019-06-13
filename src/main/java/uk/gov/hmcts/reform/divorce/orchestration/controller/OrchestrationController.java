package uk.gov.hmcts.reform.divorce.orchestration.controller;

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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseCreationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.ALLOCATED_COURT_KEY;

@Slf4j
@RestController
public class OrchestrationController {

    @Autowired
    private CaseOrchestrationService orchestrationService;

    @PutMapping(path = "/payment-update",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles payment update callbacks")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Payment update callback was processed successfully and updated "
            + " to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity paymentUpdate(@RequestBody PaymentUpdate paymentUpdate) throws WorkflowException {

        orchestrationService.update(paymentUpdate);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/submit",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles submit called from petition frontend")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Submit was successful and case was created in CCD",
            response = CaseCreationResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseCreationResponse> submit(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) throws WorkflowException {

        ResponseEntity<CaseCreationResponse> endpointResponse;

        Map<String, Object> serviceResponse = orchestrationService.submit(payload, authorizationToken);

        if (serviceResponse.containsKey(VALIDATION_ERROR_KEY)) {
            endpointResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            log.error("Bad request. Found this validation error: {}", serviceResponse.get(VALIDATION_ERROR_KEY));
        } else {
            CaseCreationResponse caseCreationResponse = new CaseCreationResponse();
            caseCreationResponse.setCaseId(String.valueOf(serviceResponse.get(ID)));
            caseCreationResponse.setStatus(SUCCESS_STATUS);

            Court allocatedCourt = (Court) serviceResponse.get(ALLOCATED_COURT_KEY);
            caseCreationResponse.setAllocatedCourt(allocatedCourt);

            endpointResponse = ResponseEntity.ok(caseCreationResponse);
        }

        return endpointResponse;
    }

    @PostMapping(path = "/updateCase/{caseId}",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles update called from petition frontend")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Update was successful and case was updated in CCD",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
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
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true)
        @RequestHeader("Authorization") final String authorizationToken)
        throws WorkflowException {

        Map<String, Object> response = orchestrationService.getDraft(authorizationToken);
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
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true)
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationToken,
        @ApiParam(value = "The case draft", required = true)
        @RequestBody
        @NotNull Map<String, Object> payload,
        @RequestParam(value = "sendEmail", required = false)
        @ApiParam(value = "Determines if the petitioner should receive the notification that the draft has been saved") final String sendEmail)
        throws WorkflowException {

        // Deprecation Warning: sendEmail as String instead of Boolean to be backwards compatible with current PFE
        return ResponseEntity.ok(orchestrationService.saveDraft(payload, authorizationToken, sendEmail));
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
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseDataResponse> retrieveAosCase(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationToken) throws WorkflowException {

        return ResponseEntity.ok(orchestrationService.retrieveAosCase(
            authorizationToken));
    }

    @GetMapping(path = "/retrieve-case", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Provides case details to front end")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case details fetched successfully",
            response = CaseDataResponse.class),
        @ApiResponse(code = 300, message = "Multiple Cases"),
        @ApiResponse(code = 404, message = "No Case found"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseDataResponse> retrieveCase(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationToken) throws WorkflowException {
        return ResponseEntity.ok(orchestrationService.getCase(authorizationToken));
    }

    @PostMapping(path = "/authenticate-respondent")
    @ApiOperation(value = "Authenticates the respondent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Respondent Authenticated"),
        @ApiResponse(code = 401, message = "User Not Authenticated"),
        @ApiResponse(code = 400, message = "Bad Request")})
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
        @ApiResponse(code = 404, message = "Case Not found")})
    public ResponseEntity<UserDetails> linkRespondent(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationToken,
        @ApiParam("Unique identifier of the session that was submitted to CCD")
        @PathVariable("caseId") String caseId,
        @ApiParam(value = "Pin", required = true)
        @PathVariable("pin") String pin) throws WorkflowException {

        UserDetails linkRespondent = orchestrationService.linkRespondent(authorizationToken, caseId, pin);

        if (linkRespondent != null) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping(path = "/petition-issue-fees",
        consumes = MediaType.APPLICATION_JSON,
        produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Return a order summary for petition issue")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Petition issue fee amount is send to CCD as callback response",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> getPetitionIssueFees(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(orchestrationService.setOrderSummary(ccdCallbackRequest))
            .build()
        );
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
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(orchestrationService.aosReceived(ccdCallbackRequest, authorizationToken));
    }

    @PostMapping(path = "/co-respondent-received")
    @ApiOperation(value = "Co-Respondent confirmation notification ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Notification sent successful"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> corespReceived(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return ResponseEntity.ok(orchestrationService.sendCoRespReceivedNotificationEmail(ccdCallbackRequest));
    }

    @PostMapping(path = "/submit-aos/{caseId}",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles respondent AOS submission")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Update was successful and case was updated in CCD",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Map<String, Object>> submitRespondentAos(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @PathVariable String caseId,
        @RequestBody @ApiParam("Complete Divorce Session / partial Aos data ") Map<String, Object> payload)
        throws WorkflowException {

        return ResponseEntity.ok(
            orchestrationService.submitRespondentAosCase(payload, authorizationToken, caseId));
    }

    @PostMapping(path = "/submit-co-respondent-aos",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles Co-Respondent AOS submission")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Update was successful and case was updated in CCD",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Map<String, Object>> submitCoRespondentAos(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @RequestBody @ApiParam("Co-respondent AOS data ") Map<String, Object> payload)
        throws WorkflowException {

        return ResponseEntity.ok(orchestrationService.submitCoRespondentAosCase(payload, authorizationToken));
    }

    @PostMapping(path = "/submit-dn/{caseId}",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handles DN update")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Update was successful and case was updated in CCD",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Map<String, Object>> submitDn(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @PathVariable String caseId,
        @RequestBody @ApiParam("Complete Divorce Session / partial DN data ") Map<String, Object> divorceSession)
        throws WorkflowException {

        return ResponseEntity.ok(
            orchestrationService.submitDnCase(divorceSession, authorizationToken, caseId));
    }

    @PutMapping(path = "/amend-petition/{caseId}")
    @ApiOperation(
        value = "Creates a new draft copy of user's old case to be amended, updates old case to AmendPetition state")
    @ApiResponses(value = {
        @ApiResponse(code = 200,
            message = "The amended petition draft has been created successfully. "
                + "The previous case has been updated to case state: AmendPetition"),
        @ApiResponse(code = 404,
            message = "No draft was created as no existing case found.")})
    public ResponseEntity<Map<String, Object>> amendPetition(@RequestHeader("Authorization")
                                                             @ApiParam(value = "JWT authorisation token issued by IDAM",
                                                                 required = true) final String authorizationToken,
                                                             @PathVariable String caseId)
        throws WorkflowException {

        return ResponseEntity.ok(orchestrationService.amendPetition(caseId, authorizationToken));
    }


}
