package uk.gov.hmcts.reform.divorce.orchestration.controller.internal;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AosInternalController {

    private final CaseOrchestrationService caseOrchestrationService;

    @PostMapping(path = "/cases/aos/make-overdue")
    @ApiOperation(value = "Moves eligible cases to AOSOverdue state")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case set as AOS Overdue"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 403, message = "Forbidden. Will only be available on non-prod environments."),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<String> markCasesForBeingMovedToAosOverdue(@RequestHeader(value = "Authorization") String authorizationToken)
        throws CaseOrchestrationServiceException {

        caseOrchestrationService.markCasesToBeMovedToAosOverdue(authorizationToken);

        return ResponseEntity.ok().build();
    }

}
