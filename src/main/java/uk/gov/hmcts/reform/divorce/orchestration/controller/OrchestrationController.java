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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CHECK_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
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

    private List<String> getErrors(Map<String, Object> response) {
        ValidationResponse validationResponse = (ValidationResponse) response.get(VALIDATION_ERROR_KEY);
        return validationResponse.getErrors();
    }
}
