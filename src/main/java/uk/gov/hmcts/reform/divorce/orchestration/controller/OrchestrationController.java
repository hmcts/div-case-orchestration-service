package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DELETE_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SAVE_DRAFT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

@Slf4j
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON)
public class OrchestrationController {

    @Autowired
    private CaseOrchestrationService orchestrationService;

    @PostMapping(path = "/petition-issued", consumes = MediaType.APPLICATION_JSON)
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
            @RequestBody @ApiParam("CaseData") CreateEvent caseDetailsRequest) {
        Map<String, Object> response;
        try {
            response = orchestrationService.ccdCallbackHandler(caseDetailsRequest, authorizationToken);
        } catch (WorkflowException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

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

    @PostMapping(path = "/submit")
    @ApiOperation(value = "Handles submit called from petition frontend")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Submit was successful and case was created in CCD",
                    response = CaseResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<CaseResponse> submit(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) {
        Map<String, Object> response;
        try {
            response = orchestrationService.submit(payload, authorizationToken);
        } catch (WorkflowException exception) {
            log.error(exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (response.containsKey(VALIDATION_ERROR_KEY)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(
                CaseResponse.builder()
                        .caseId(response.get(ID).toString())
                        .status(SUCCESS_STATUS)
                        .build());
    }

    @PostMapping(path = "/updateCase/{caseId}/{eventId}")
    @ApiOperation(value = "Handles update called from petition frontend")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Update was successful and case was updated in CCD",
                    response = CaseResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")
            })
    public ResponseEntity<CaseResponse> update(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @PathVariable String caseId,
            @PathVariable String eventId,
            @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) {
        Map<String, Object> response;
        try {
            response = orchestrationService.update(payload, authorizationToken, caseId, eventId);
        } catch (WorkflowException exception) {
            log.error(exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(
                CaseResponse.builder()
                        .caseId(response.get(ID).toString())
                        .status(SUCCESS_STATUS)
                        .build());
    }
    
    @GetMapping(path = "/draft", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieves a divorce case draft")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A draft exists. The draft content is in the response body"),
            @ApiResponse(code = 404, message = "Draft does not exist")})
    public ResponseEntity<Map<String, Object>> retrieveDraft(
            @RequestHeader("Authorization") @ApiParam(value = "JWT authorisation token issued by IDAM", required = true)
            final String authorizationToken) {
        Map<String, Object> response;
        try {
            response = orchestrationService.getDraft(authorizationToken);
        } catch (WorkflowException e) {
            log.error("Error retrieving draft" , e);
            throw new RuntimeException(e);
        }
        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/drafts", consumes = MediaType.APPLICATION_JSON)
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
            @Email final String notificationEmail) {

        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response = orchestrationService.saveDraft(payload, authorizationToken, notificationEmail);
        } catch (WorkflowException e) {
            log.error("Error saving draft", e);
            response = new HashMap<>();
            response.put(SAVE_DRAFT_ERROR_KEY, e);
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/drafts")
    @ApiOperation(value = "Deletes a divorce case draft")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The divorce draft has been deleted successfully")})
    public ResponseEntity<Map<String, Object>> deleteDraft(@RequestHeader("Authorization")
                                            @ApiParam(value = "JWT authorisation token issued by IDAM",
                                                    required = true) final String authorizationToken) {

        Map<String, Object> response;
        try {
            response  = orchestrationService.deleteDraft(authorizationToken);
        } catch (WorkflowException e) {
            log.error("Error deleting draft", e);
            response = new LinkedHashMap<>();
            response.put(DELETE_ERROR_KEY, e);
        }

        return ResponseEntity.ok(response);
    }

    private List<String> getErrors(Map<String, Object> response) {
        ValidationResponse validationResponse = (ValidationResponse) response.get(VALIDATION_ERROR_KEY);
        return validationResponse.getErrors();
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
            log.error(e.getMessage());
        }

        if (authenticateRespondent != null && authenticateRespondent) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
