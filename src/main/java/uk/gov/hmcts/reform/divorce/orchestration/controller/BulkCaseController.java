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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkCaseService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@Slf4j
@AllArgsConstructor
public class BulkCaseController {

    private final CaseOrchestrationService orchestrationService;

    private final BulkCaseService bulkCaseService;

    @PostMapping(path = "/bulk/case")
    @ApiOperation(value = "Create bulk case ready for listing")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Bulk case for listing created"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Map<String, Object>> createBulkCase() throws WorkflowException {

        Map<String, Object> response = orchestrationService.generateBulkCaseForListing();
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/bulk/schedule/listing")
    @ApiOperation(value = "Callback to begin processing cases in bulk case for the court hearing")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Bulk case processing has been initiated"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> scheduleBulkCaseForHearing(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM") final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        CcdCallbackResponse.CcdCallbackResponseBuilder ccdCallbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            orchestrationService.processBulkCaseScheduleForHearing(ccdCallbackRequest, authorizationToken);
        } catch (WorkflowException exception) {
            ccdCallbackResponseBuilder.errors(asList(exception.getMessage()));
        }

        return ResponseEntity.ok(ccdCallbackResponseBuilder.build());
    }

    @PostMapping(path = "/bulk/validate/listing")
    @ApiOperation(value = "Validate bulk case data for listing")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Bulk case validated for listing"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> validateBulkCaseListingData(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        CcdCallbackResponse.CcdCallbackResponseBuilder ccdCallbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            orchestrationService.validateBulkCaseListingData(ccdCallbackRequest.getCaseDetails().getCaseData());
        } catch (WorkflowException exception) {
            ccdCallbackResponseBuilder.errors(asList(exception.getMessage()));
        }

        return ResponseEntity.ok(ccdCallbackResponseBuilder.build());
    }

    @PostMapping(path = "/bulk/edit/listing")
    @ApiOperation(value = "Callback to validate bulk case data for listing")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Bulk case processing has been initiated"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> editBulkCaseListingData(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM") final String authorizationToken,
        @RequestParam(value = "templateId") @ApiParam("templateId") String templateId,
        @RequestParam(value = "documentType") @ApiParam("documentType") String documentType,
        @RequestParam(value = "filename") @ApiParam("filename") String filename,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        CcdCallbackResponse.CcdCallbackResponseBuilder ccdCallbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            Map<String, Object> response = orchestrationService.editBulkCaseListingData(ccdCallbackRequest,
                filename, templateId, documentType, authorizationToken);
            ccdCallbackResponseBuilder.data(response);
        } catch (WorkflowException exception) {
            log.error("Error validating bulk case with BulkCaseId : {}", ccdCallbackRequest.getCaseDetails().getCaseId(), exception);
            ccdCallbackResponseBuilder.errors(asList(exception.getMessage()));
        }

        return ResponseEntity.ok(ccdCallbackResponseBuilder.build());
    }

    @PostMapping(path = "/bulk/pronounce/submit")
    @ApiOperation(value = "Callback to set required data on case when DN Pronounced")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Required pronouncement data has been set successfully"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> updateCaseDnPronounce(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM") final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        CcdCallbackResponse.CcdCallbackResponseBuilder ccdCallbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            ccdCallbackResponseBuilder.data(orchestrationService
                .updateBulkCaseDnPronounce(ccdCallbackRequest.getCaseDetails(), authorizationToken));
        } catch (WorkflowException exception) {
            ccdCallbackResponseBuilder.errors(asList(exception.getMessage()));
        }

        return ResponseEntity.ok(ccdCallbackResponseBuilder.build());
    }

    @PostMapping(path = "/bulk/remove")
    @ApiOperation(value = "Callback to remove cases from the bulk")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Cases have been removed successfully"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> removeCasesFromBulk(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM") final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        CcdCallbackResponse.CcdCallbackResponseBuilder ccdCallbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            ccdCallbackResponseBuilder.data(orchestrationService
                .updateBulkCaseAcceptedCases(ccdCallbackRequest.getCaseDetails(), authorizationToken));
        } catch (WorkflowException exception) {
            ccdCallbackResponseBuilder.errors(asList(exception.getMessage()));
        }

        return ResponseEntity.ok(ccdCallbackResponseBuilder.build());
    }

    @PostMapping(path = "/bulk/remove/listing")
    @ApiOperation(value = "Callback to remove cases from the bulk listing")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Cases have been removed from listed bulk case successfully"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> removeCasesFromBulkListed(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM") final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        CcdCallbackResponse.CcdCallbackResponseBuilder ccdCallbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            ccdCallbackResponseBuilder.data(bulkCaseService.removeFromBulkListed(ccdCallbackRequest, authorizationToken));
        } catch (WorkflowException exception) {
            ccdCallbackResponseBuilder.errors(asList(exception.getMessage()));
        }

        return ResponseEntity.ok(ccdCallbackResponseBuilder.build());
    }

    @PostMapping(path = "/bulk/pronouncement/cancel")
    @ApiOperation(value = "Callback to cancel bulk pronouncement")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Cases to cancel pronouncement"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> cancelBulkPronouncement(
        @RequestHeader(AUTHORIZATION_HEADER)
        @ApiParam(value = "Authorisation token issued by IDAM") final String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        CcdCallbackResponse.CcdCallbackResponseBuilder ccdCallbackResponseBuilder = CcdCallbackResponse.builder();

        try {
            orchestrationService.processCancelBulkCasePronouncement(ccdCallbackRequest, authorizationToken);
        } catch (WorkflowException exception) {
            ccdCallbackResponseBuilder.errors(asList(exception.getMessage()));
        }

        return ResponseEntity.ok(ccdCallbackResponseBuilder.build());
    }
}
