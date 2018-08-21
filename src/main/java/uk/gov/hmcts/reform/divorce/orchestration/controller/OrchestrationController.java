package uk.gov.hmcts.reform.divorce.orchestration.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

@Slf4j
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
public class OrchestrationController {

    @Autowired
    CaseOrchestrationService orchestrationService;

    @PostMapping(path = "/submit")
    @ApiOperation(value = "Handles submit from called from petition frontend")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Submit was successful and case was created in CCD",
                    response = CcdCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")
                                    })
    public ResponseEntity<Map<String, Object>> submit(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @RequestBody @ApiParam("Divorce Session") Map<String, Object> payLoad) {
        try {
            payLoad = orchestrationService.submit(payLoad, authorizationToken);
        } catch (WorkflowException e) {
            log.error(e.getMessage());
        }

        return ResponseEntity.ok(payLoad);
    }

    @PostMapping(path = "/petition-issued")
    @ApiOperation(value = "Handles Issue callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case",
                    response = CcdCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")
                                    })
    public ResponseEntity<CcdCallbackResponse> petitionIssuedCallback(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @RequestBody @ApiParam("CaseData") CreateEvent caseDetailsRequest) {
        Map<String, Object> response = null;
        try {
            response = orchestrationService.ccdCallbackHandler(caseDetailsRequest, authorizationToken);
        } catch (WorkflowException e) {
            log.error(e.getMessage());
        }

        if(response.containsKey(VALIDATION_ERROR_KEY)){
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

    private List<String> getErrors(Map<String, Object> response) {
        ValidationResponse validationResponse = (ValidationResponse) response.get(VALIDATION_ERROR_KEY);
        return validationResponse.getErrors();
    }
}
