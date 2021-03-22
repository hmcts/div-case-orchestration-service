package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeNisiService;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@Slf4j
@AllArgsConstructor
public class DecreeNisiController {

    private final DecreeNisiService decreeNisiService;

    @PostMapping(path = "/dn-pronounced-manual", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Task to set Decree Nisi as pronounced for one case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "DN has been pronounced for this case",
            response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> dnPronouncedManual(
        @RequestHeader(value = AUTHORIZATION_HEADER)
        @ApiParam(value = "JWT auth token issued by IDAM", required = true) final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();
        try {
            callbackResponseBuilder.data(decreeNisiService.setDNGrantedManual(ccdCallbackRequest, authorizationToken));
            log.info("DN pronounced for case with ID: {}.", caseId);
        } catch (WorkflowException exception) {
            log.error("DN pronouncement has failed for case with ID: {}", caseId, exception);
            callbackResponseBuilder.errors(singletonList(exception.getMessage()));
        }
        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

    @PostMapping(path = "/generate-manual-dn-pronouncement-documents", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Generate the documents for Decree Nisi Pronouncement and attach them to the case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "DN Pronouncement documents have been attached to the case", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> generateManualDnDocuments(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(
                decreeNisiService.handleManualDnPronouncementDocumentGeneration(ccdCallbackRequest, authorizationToken));
            log.info("Generated DN documents for Case ID: {}.", caseId);
        } catch (WorkflowException exception) {
            log.error("DN document generation failed for Case ID: {}", caseId, exception);
            callbackResponseBuilder.errors(Collections.singletonList(exception.getMessage()));
        }

        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

}
