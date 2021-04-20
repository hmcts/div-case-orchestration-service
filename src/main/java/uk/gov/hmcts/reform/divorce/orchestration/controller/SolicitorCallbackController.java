package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.SolicitorService;

import java.util.Map;
import javax.ws.rs.core.MediaType;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.isSolicitorPaymentMethodPba;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ControllerUtils.responseWithData;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ControllerUtils.responseWithErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SolicitorCallbackController {

    private final SolicitorService solicitorService;

    @PostMapping(path = "/personal-service-pack",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Validates case to be issued with personal service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case validation successful",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> issuePersonalServicePack(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        Map<String, Object> response;

        try {
            response = solicitorService.validateForPersonalServicePack(ccdCallbackRequest, authorizationToken);
        } catch (Exception e) {
            return ResponseEntity.ok(
                CcdCallbackResponse.builder()
                    .data(ImmutableMap.of())
                    .warnings(ImmutableList.of())
                    .errors(singletonList("Failed to issue solicitor personal service - " + e.getMessage()))
                    .build());
        }
        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(response)
                .build());
    }

    @PostMapping(path = "/solicitor-confirm-service",
        consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Validates the case for solicitor confirm service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case successfully validated",
            response = CaseResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> solicitorConfirmPersonalService(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        Map<String, Object> response;
        try {
            response = solicitorService.solicitorConfirmPersonalService(ccdCallbackRequest);
        } catch (Exception e) {
            return ResponseEntity.ok(
                CcdCallbackResponse.builder()
                    .data(ImmutableMap.of())
                    .warnings(ImmutableList.of())
                    .errors(singletonList("Failed to validate for solicitor confirm personal service - " + e.getMessage()))
                    .build());
        }
        return ResponseEntity.ok(
            CcdCallbackResponse.builder()
                .data(response)
                .build());
    }


    @PostMapping(path = "/handle-post-personal-service-pack")
    @ApiOperation(value = "Callback to notify solicitor that personal service pack has been issued")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed")})
    public ResponseEntity<CcdCallbackResponse> sendSolicitorPersonalServiceEmail(
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(solicitorService.sendSolicitorPersonalServiceEmail(ccdCallbackRequest))
            .build());
    }

    @PostMapping(path = "/retrieve-pba-numbers")
    @ApiOperation(value = "Callback to retrieve PBA numbers for a given solicitor")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed")})
    public ResponseEntity<CcdCallbackResponse> retrievePbaNumbers(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorizationToken,
        @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();

        if (!isSolicitorPaymentMethodPba(caseData)) {
            log.info("Case ID: {}. /retrieve-pba-numbers called but for payment method other than PBA", caseId);
            return responseWithData(caseData);
        }

        Map<String, Object> response = solicitorService.retrievePbaNumbers(ccdCallbackRequest, authorizationToken);

        if (null == response.get(PBA_NUMBERS)) {
            return responseWithErrors(asList("No PBA number found for this account, please contact your organisation."));
        }
        return responseWithData(response);
    }
}
