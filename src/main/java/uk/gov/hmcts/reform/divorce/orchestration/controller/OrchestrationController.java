package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import javax.ws.rs.core.MediaType;
import java.util.Map;

@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
public class OrchestrationController {

    @Autowired
    CaseOrchestrationService orchestrationService;

    @PostMapping(path = "/submit")
    @ApiOperation(value = "Handles submit from called from petition frontend")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Submit was successful and case was created in CCD",
            response = CCDCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")
    }
    )
    public ResponseEntity<Map<String, Object>> petitionIssued(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @RequestBody @ApiParam("CaseData") CreateEvent caseDetailsRequest) {
        Map<String, Object> submit = null;
        try {
             submit = orchestrationService.submit(null, authorizationToken);
        } catch (WorkflowException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(submit);
    }
}
