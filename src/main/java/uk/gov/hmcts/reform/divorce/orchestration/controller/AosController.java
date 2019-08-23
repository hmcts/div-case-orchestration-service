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
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;

import javax.ws.rs.core.MediaType;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * Controller class for aos endpoints.
 */
@RestController
@Slf4j
public class AosController {
    @Autowired
    private AosService aosService;


    @PostMapping(path = "/aos-overdue",
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Generate/dispatch a notification email to the petitioner when their case is moved to AOS overdue")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "An email notification has been generated and dispatched",
                    response = CcdCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> notifyPetitionerOfAOSOverdue(
            @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.info("/aos-overdue endpoint called for caseId {}", caseId);
        Map<String, Object> returnedCaseData;

        try {
            returnedCaseData = aosService.sendPetitionerAOSOverdueNotificationEmail(ccdCallbackRequest);
        } catch (WorkflowException e) {
            log.error("Failed to call service for caseId {}", caseId, e);
            return ResponseEntity.ok(CcdCallbackResponse.builder()
                    .errors(singletonList(e.getMessage()))
                    .build());
        }

        return ResponseEntity.ok(CcdCallbackResponse.builder()
                .data(returnedCaseData)
                .build());
    }
}
