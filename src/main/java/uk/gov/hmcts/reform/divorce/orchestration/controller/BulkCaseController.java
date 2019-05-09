package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import javax.ws.rs.Path;
import java.util.Map;

@RestController
@Slf4j
@Path("/bulk")
@AllArgsConstructor
public class BulkCaseController {
    @Autowired
    private final CaseOrchestrationService orchestrationService;

    @GetMapping(path = "/process-cases-ready-for-listing")
    @ApiOperation(value = "Process cases ready for listing")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Bulk case for listing created"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Map<String, Object>> processCasesReadyForListing() throws WorkflowException {

        Map<String, Object> response = orchestrationService.generateBulkCaseForListing();
        return ResponseEntity.ok(response);
    }
}
