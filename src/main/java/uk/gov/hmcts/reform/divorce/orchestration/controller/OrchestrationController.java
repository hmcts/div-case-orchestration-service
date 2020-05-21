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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;
import javax.validation.constraints.NotNull;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.ALLOCATED_COURT_KEY;

@Slf4j
@RestController
public class OrchestrationController {

    @Autowired
    private CaseOrchestrationService orchestrationService;

    @Autowired
    private AuthUtil authUtil;

    @PutMapping(path = "/payment-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Handles Payment Update callbacks")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Payment update callback was processed successfully and updated to the case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
        @ApiResponse(code = 403, message = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity paymentUpdate(
        @RequestHeader(value = SERVICE_AUTHORIZATION_HEADER) String s2sAuthToken,
        @RequestBody PaymentUpdate paymentUpdate) throws WorkflowException {

        authUtil.assertIsServiceAllowedToUpdate(s2sAuthToken);

        orchestrationService.update(paymentUpdate);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Create case in CCD - called by Petitioner Frontend")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Submit was successful and a case was created in CCD",
            response = CaseCreationResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseCreationResponse> submit(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
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

    @PostMapping(path = "/updateCase/{caseId}", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Update case in CCD - called by Frontend applications")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Update was successful and case was updated in CCD",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseResponse> update(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @PathVariable String caseId,
        @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) throws WorkflowException {

        return ResponseEntity.ok(
            CaseResponse.builder()
                .caseId(orchestrationService.update(payload, authorizationToken, caseId).get(ID).toString())
                .status(SUCCESS_STATUS)
                .build());
    }

    @GetMapping(path = "/draftsapi/version/1", produces = APPLICATION_JSON)
    @ApiOperation(value = "Retrieves a divorce case draft")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A draft exists. The draft content is in the response body"),
        @ApiResponse(code = 404, message = "Draft does not exist")})
    public ResponseEntity<Map<String, Object>> retrieveDraft(
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true)
        @RequestHeader(AUTHORIZATION_HEADER) final String authorizationToken)
        throws WorkflowException {

        Map<String, Object> response = orchestrationService.getDraft(authorizationToken);
        if (MapUtils.isEmpty(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/draftsapi/version/1", consumes = APPLICATION_JSON)
    @ApiOperation(value = "Saves or updates a draft case to Draft Store")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Draft saved")})
    public ResponseEntity<Map<String, Object>> saveDraft(
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true)
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationToken,
        @ApiParam(value = "The draft case", required = true)
        @RequestBody
        @NotNull Map<String, Object> payload,
        @RequestParam(value = "sendEmail", required = false)
        @ApiParam(value = "Determines if the petitioner should receive the notification that the draft has been saved") final String sendEmail)
        throws WorkflowException {

        // Deprecation Warning: sendEmail as String instead of Boolean to be backwards compatible with current PFE
        return ResponseEntity.ok(orchestrationService.saveDraft(payload, authorizationToken, sendEmail));
    }

    @DeleteMapping(path = "/draftsapi/version/1")
    @ApiOperation(value = "Deletes a Divorce draft case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The Divorce draft case has been deleted successfully")})
    public ResponseEntity<Map<String, Object>> deleteDraft(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken) throws WorkflowException {

        return ResponseEntity.ok(orchestrationService.deleteDraft(authorizationToken));
    }

    @GetMapping(path = "/retrieve-aos-case", produces = APPLICATION_JSON)
    @ApiOperation(value = "Provides case details to Frontend Applications")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case details fetched successfully",
            response = CaseDataResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseDataResponse> retrieveAosCase(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationToken) throws WorkflowException {

        return ResponseEntity.ok(orchestrationService.retrieveAosCase(
            authorizationToken));
    }

    @GetMapping(path = "/retrieve-case", produces = APPLICATION_JSON)
    @ApiOperation(value = "Provides case details to Frontend Applications")
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
        @ApiParam("Unique ID of the session that was submitted to CCD")
        @PathVariable("caseId") String caseId,
        @ApiParam(value = "Respondent Pin Code", required = true)
        @PathVariable("pin") String pin) throws WorkflowException {

        UserDetails linkRespondent = orchestrationService.linkRespondent(authorizationToken, caseId, pin);

        if (linkRespondent != null) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping(path = "/submit-aos/{caseId}", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Submit AOS answers and update original Divorce case with new data")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "AOS successfully submitted and original case was updated in CCD",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Map<String, Object>> submitRespondentAos(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @PathVariable String caseId,
        @RequestBody @ApiParam("Complete Divorce Session / partial AoS data ") Map<String, Object> payload)
        throws WorkflowException {

        return ResponseEntity.ok(
            orchestrationService.submitRespondentAosCase(payload, authorizationToken, caseId));
    }

    @PostMapping(path = "/submit-co-respondent-aos", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Submit Co-Respondents AOS answers and update original Divorce case with new data")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "AOS successfully submitted for Co-Respondent and original case was updated in CCD",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Map<String, Object>> submitCoRespondentAos(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("Co-Respondent AOS data") Map<String, Object> payload)
        throws WorkflowException {

        return ResponseEntity.ok(orchestrationService.submitCoRespondentAosCase(payload, authorizationToken));
    }

    @PostMapping(path = "/submit-dn/{caseId}", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Submit Decree Nisi answers and update original Divorce case with new data")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "DN successfully submitted and case was updated in CCD",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Map<String, Object>> submitDn(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @PathVariable String caseId,
        @RequestBody @ApiParam("Complete Divorce Session / partial DN data ") Map<String, Object> divorceSession)
        throws WorkflowException {

        return ResponseEntity.ok(
            orchestrationService.submitDnCase(divorceSession, authorizationToken, caseId));
    }

    @PostMapping(path = "/submit-da/{caseId}", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Submit Decree Absolute answers and update original Divorce case with new data")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "DN successfully submitted and case was updated in CCD",
                    response = CaseResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Map<String, Object>> submitDa(
            @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
            @PathVariable String caseId,
            @RequestBody @ApiParam("Decree Absolute Data as required") Map<String, Object> divorceSession)
            throws WorkflowException {

        return ResponseEntity.ok(
                orchestrationService.submitDaCase(divorceSession, authorizationToken, caseId));
    }

    @PutMapping(path = "/amend-petition/{caseId}")
    @ApiOperation(
        value = "Creates a new draft copy of user's old case to be amended, updates old case to AmendPetition state")
    @ApiResponses(value = {
        @ApiResponse(code = 200,
            message = "The amended petition draft has been created successfully. The previous case has been updated to case state: AmendPetition"),
        @ApiResponse(code = 404,
            message = "No draft was created as no existing case found.")})
    public ResponseEntity<Map<String, Object>> amendPetition(
            @RequestHeader(AUTHORIZATION_HEADER)
            @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
            @PathVariable String caseId)
            throws WorkflowException {

        return ResponseEntity.ok(orchestrationService.amendPetition(caseId, authorizationToken));
    }

    @PutMapping(path = "/amend-petition-dn-rejection/{caseId}")
    @ApiOperation(
        value = "Creates a new draft copy of user's old case to be amended for DN Refusal Rejection, updates old case to AmendPetition state")
    @ApiResponses(value = {
        @ApiResponse(code = 200,
            message = "The amended petition draft has been created successfully. The previous case has been updated to case state: AmendPetition"),
        @ApiResponse(code = 404,
            message = "No draft was created as no existing case found.")})
    public ResponseEntity<Map<String, Object>> amendPetitionForRefusal(
            @RequestHeader(AUTHORIZATION_HEADER)
            @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String authorizationToken,
            @PathVariable String caseId)
            throws WorkflowException {

        return ResponseEntity.ok(orchestrationService.amendPetitionForRefusal(caseId, authorizationToken));
    }
}