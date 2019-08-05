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
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;

import javax.ws.rs.core.MediaType;

import static java.util.Arrays.asList;

@RestController
@Slf4j
@AllArgsConstructor
public class DecreeAbsoluteController {

    private final DecreeAbsoluteService decreeAbsoluteService;

    @PostMapping(path = "/da-requested-by-applicant", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Handle email notification to respondent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "email sent.", response = CcdCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CcdCallbackResponse> notifyRespondentOfDARequested(
        @RequestHeader(value = "Authorization") String authorizationToken  ,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        CcdCallbackResponse.CcdCallbackResponseBuilder callbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            callbackResponseBuilder.data(decreeAbsoluteService.notifyRespondentOfDARequested(ccdCallbackRequest, authorizationToken));
            log.info("Emailed Respondent as applicant has requested DA for case {}.", caseId);
        } catch (WorkflowException exception) {
            log.error("Respondent notification email failed to send for applicant requesting DA. Case id: {}", caseId, exception);
            callbackResponseBuilder.errors(asList(exception.getMessage()));

        }
        return ResponseEntity.ok(callbackResponseBuilder.build());
    }

}
