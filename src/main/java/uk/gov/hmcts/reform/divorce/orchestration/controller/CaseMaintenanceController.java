package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseMaintenanceService;

import java.util.Map;
import javax.ws.rs.core.MediaType;

@Slf4j
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
public class CaseMaintenanceController {

    @Autowired
    CaseMaintenanceService caseMaintenanceService;

    @PostMapping(path = "/submit")
    @ApiOperation(value = "Handles submit called from petition frontend")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Submit was successful and case was created in CCD",
            response = CCDCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")
        })
    public ResponseEntity<Map<String, Object>> submit(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) {
        try {
            payload = caseMaintenanceService.submit(payload, authorizationToken);
        } catch (WorkflowException e) {
            log.error(e.getMessage());
        }

        return ResponseEntity.ok(payload);
    }

    @PostMapping(path = "/updateCase/{caseId}/{eventId}")
    @ApiOperation(value = "Handles update called from petition frontend")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Update was successful and case was updated in CCD",
                response = CCDCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")
        })
    public ResponseEntity<Map<String, Object>> update(
        @PathVariable String caseId,
        @PathVariable String eventId,
        @RequestHeader(value = "Authorization") String authorizationToken,
        @RequestBody @ApiParam("Divorce Session") Map<String, Object> payload) {
        try {
            payload = caseMaintenanceService.update(payload, authorizationToken, caseId, eventId);
        } catch (WorkflowException e) {
            log.error(e.getMessage());
        }

        return ResponseEntity.ok(payload);
    }
}
