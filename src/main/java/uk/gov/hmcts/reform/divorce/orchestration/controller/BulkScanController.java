package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.bsp.common.config.BulkScanEndpoints;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.CaseDetails;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.SuccessfulTransformationResponse;
import uk.gov.hmcts.reform.bsp.common.model.update.in.BulkScanCaseUpdateRequest;
import uk.gov.hmcts.reform.bsp.common.model.update.output.SuccessfulUpdateResponse;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataValidationRequest;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResponse;
import uk.gov.hmcts.reform.bsp.common.service.AuthService;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulkscan.BulkScanEvents;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.BulkScanService;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;

@Slf4j
@Controller
public class BulkScanController {

    @Autowired
    private BulkScanService bulkScanService;

    @Autowired
    private AuthService authService;

    @PostMapping(path = BulkScanEndpoints.VALIDATE, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation("Validates OCR form data based on form type")
    @ApiResponses( {
        @ApiResponse(code = 200, response = OcrValidationResponse.class, message = "Validation executed successfully"),
        @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
        @ApiResponse(code = 403, message = "S2S token is not authorized to use the service"),
        @ApiResponse(code = 404, message = "Form type not found")
    })
    public ResponseEntity<OcrValidationResponse> validateOcrData(
        @RequestHeader(name = SERVICE_AUTHORIZATION_HEADER) String s2sAuthToken,
        @PathVariable(name = "form-type") String formType,
        @Valid @RequestBody OcrDataValidationRequest request
    ) {
        log.info("Validating form {} for bulk scanning operation", formType);
        authService.assertIsServiceAllowedToValidate(s2sAuthToken);
        ResponseEntity<OcrValidationResponse> response;

        try {
            OcrValidationResponse ocrValidationResponse = validateExceptionRecord(formType, request.getOcrDataFields());
            response = ok().body(ocrValidationResponse);
        } catch (UnsupportedFormTypeException unsupportedFormTypeException) {
            log.error(unsupportedFormTypeException.getMessage(), unsupportedFormTypeException);
            response = ResponseEntity.notFound().build();
        }

        return response;
    }

    @PostMapping(path = BulkScanEndpoints.TRANSFORM, consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Transform exception record into CCD case data")
    @ApiResponses( {
        @ApiResponse(code = 200, response = SuccessfulTransformationResponse.class,
            message = "Transformation of Exception Record into CCD Case Data has been successful"),
        @ApiResponse(code = 400, message = "Request failed due to malformed syntax (and only for that reason)"),
        @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
        @ApiResponse(code = 403, message = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(code = 422, message = "Exception Record is well-formed, but contains invalid data.")
    })
    public ResponseEntity<SuccessfulTransformationResponse> transformExceptionRecordIntoCase(
        @RequestHeader(name = SERVICE_AUTHORIZATION_HEADER) String s2sAuthToken,
        @Valid @RequestBody ExceptionRecord exceptionRecord) {
        String exceptionRecordId = exceptionRecord.getId();
        log.info("Transforming Exception Record to case with Case ID: {}", exceptionRecordId);
        authService.assertIsServiceAllowedToUpdate(s2sAuthToken);
        ResponseEntity<SuccessfulTransformationResponse> controllerResponse;

        try {
            Map<String, Object> transformedCaseData = bulkScanService.transformBulkScanForm(exceptionRecord);

            SuccessfulTransformationResponse callbackResponse = SuccessfulTransformationResponse.builder()
                .caseCreationDetails(
                    new CaseCreationDetails(
                        CASE_TYPE_ID,
                        BulkScanEvents.CREATE.getEventName(),
                        transformedCaseData
                    )
                )
                .build();

            controllerResponse = ok(callbackResponse);
        } catch (UnsupportedFormTypeException exception) {
            log.error(format("Error transforming Exception Record. Exception record ID is: %s", exceptionRecordId), exception);
            controllerResponse = ResponseEntity.unprocessableEntity().build();
        }

        return controllerResponse;
    }

    @PostMapping(path = BulkScanEndpoints.UPDATE, consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Update Divorce CCD case data by bulk scan")
    @ApiResponses( {
        @ApiResponse(code = 200, response = SuccessfulTransformationResponse.class, message = "CCD Case successfully updated by bulk scan"),
        @ApiResponse(code = 400, message = "Request failed due to malformed syntax (and only for that reason). "
            + "This response results in a general error presented to the caseworker in CCD"),
        @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
        @ApiResponse(code = 403, message = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(code = 422, message = "Exception record is well-formed, but contains invalid data")
    })
    public ResponseEntity<SuccessfulUpdateResponse> updateCase(
        @RequestHeader(name = SERVICE_AUTHORIZATION_HEADER) String s2sAuthToken,
        @Valid @RequestBody BulkScanCaseUpdateRequest request) {
        authService.assertIsServiceAllowedToUpdate(s2sAuthToken);
        ResponseEntity<SuccessfulUpdateResponse> updateControllerResponse;

        try {
            CaseDetails updatedCase = bulkScanService.transformExceptionRecordAndUpdateExistingCase(
                request.getExceptionRecord(), request.getCaseDetails()
            );

            SuccessfulUpdateResponse callbackResponse = SuccessfulUpdateResponse.builder()
                .caseDetails(
                    CaseDetails.builder()
                        .caseTypeId(CASE_TYPE_ID)
                        .caseData(updatedCase.getCaseData())
                        // here we need to send new state to RBS. RBS hasn't implemented it yet
                        .build()
                ).build();

            updateControllerResponse = ok(callbackResponse);
        } catch (UnsupportedFormTypeException exception) {
            updateControllerResponse = ResponseEntity.unprocessableEntity().build();
        }

        return updateControllerResponse;
    }

    private OcrValidationResponse validateExceptionRecord(String formType, List<OcrDataField> ocrDataFields) {
        return new OcrValidationResponse(bulkScanService.validateBulkScanForm(formType, ocrDataFields));
    }
}
