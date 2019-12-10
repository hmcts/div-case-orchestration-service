package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.bsp.common.service.AuthService;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.out.CaseCreationDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.out.SuccessfulTransformationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.update.in.BulkScanCaseUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.update.out.SuccessfulUpdateResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.OcrValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.divorce.orchestration.exception.bulk.scan.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.BulkScanService;

import java.util.Collections;
import java.util.Map;

import javax.validation.Valid;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@Controller
public class BulkScanController {

    private static final String CASE_TYPE_ID = "DIVORCE";
    private static final String CREATE_EVENT_ID = "bulkScanCaseCreate";
    private static final String UPDATE_EVENT_ID = "bulkScanCaseUpdate";
    public static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";

    @Autowired
    private BulkScanService bulkScanService;

    @Autowired
    private AuthService authService;

    @PostMapping(
        path = "/forms/{form-type}/validate-ocr",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation("Validates OCR form data based on form type")
    @ApiResponses( {
        @ApiResponse(
            code = 200, response = OcrValidationResponse.class, message = "Validation executed successfully"
        ),
        @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
        @ApiResponse(code = 403, message = "S2S token is not authorized to use the service"),
        @ApiResponse(code = 404, message = "Form type not found")
    })
    public ResponseEntity<OcrValidationResponse> validateOcrData(
        @RequestHeader(name = SERVICE_AUTHORISATION_HEADER) String s2sAuthToken,
        @PathVariable(name = "form-type") String formType,
        @Valid @RequestBody OcrDataValidationRequest request
    ) {
        log.info("Validating form {} for bulk scanning operation", formType);

        authService.assertIsServiceAllowedToValidate(s2sAuthToken);

        ResponseEntity<OcrValidationResponse> response;

        try {
            OcrValidationResult ocrValidationResult = bulkScanService
                .validateBulkScanForm(formType, request.getOcrDataFields());
            OcrValidationResponse ocrValidationResponse = new OcrValidationResponse(ocrValidationResult);
            response = ok().body(ocrValidationResponse);
        } catch (UnsupportedFormTypeException unsupportedFormTypeException) {
            log.error(unsupportedFormTypeException.getMessage(), unsupportedFormTypeException);
            response = ResponseEntity.notFound().build();
        }

        return response;
    }

    @PostMapping(
        path = "/transform-exception-record",
        consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON
    )
    @ApiOperation(value = "Transform exception record into CCD case data")
    @ApiResponses( {
        @ApiResponse(code = 200, response = SuccessfulTransformationResponse.class,
            message = "Transformation of exception record into case data has been successful"
        ),
        @ApiResponse(code = 400, message = "Request failed due to malformed syntax (and only for that reason)"),
        @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
        @ApiResponse(code = 403, message = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(code = 422, message = "Exception record is well-formed, but contains invalid data.")
    })
    public ResponseEntity<SuccessfulTransformationResponse> transformExceptionRecordIntoCase(
        @RequestHeader(name = SERVICE_AUTHORISATION_HEADER) String s2sAuthToken,
        @Valid @RequestBody ExceptionRecord exceptionRecord
    ) {
        log.info("Transforming exception record to case");

        authService.assertIsServiceAllowedToUpdate(s2sAuthToken);

        ResponseEntity<SuccessfulTransformationResponse> controllerResponse;
        try {
            Map<String, Object> transformedCaseData = bulkScanService.transformBulkScanForm(exceptionRecord);

            SuccessfulTransformationResponse callbackResponse = SuccessfulTransformationResponse.builder()
                .caseCreationDetails(
                    new CaseCreationDetails(
                        CASE_TYPE_ID,
                        CREATE_EVENT_ID,
                        transformedCaseData))
                .build();

            controllerResponse = ok(callbackResponse);
        } catch (UnsupportedFormTypeException exception) {
            controllerResponse = ResponseEntity.unprocessableEntity().build();
        }

        return controllerResponse;
    }

    @PostMapping(
        path = "/update-case",
        consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON
    )
    @ApiOperation(value = "API to update Divorce case data by bulk scan")
    @ApiResponses( {
        @ApiResponse(code = 200, response = SuccessfulTransformationResponse.class,
            message = "Update of case data has been successful"
        ),
        @ApiResponse(code = 400, message = "Request failed due to malformed syntax (and only for that reason). "
            + "This response results in a general error presented to the caseworker in CCD"),
        @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
        @ApiResponse(code = 403, message = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(code = 422, message = "Exception record is well-formed, but contains invalid data.")
    })
    public ResponseEntity<SuccessfulUpdateResponse> updateCase(
        @RequestHeader(name = SERVICE_AUTHORISATION_HEADER) String s2sAuthToken,
        @Valid @RequestBody BulkScanCaseUpdateRequest request
    ) {
        log.info("Updates existing case based on exception record");

        authService.assertIsServiceAllowedToUpdate(s2sAuthToken);

        SuccessfulUpdateResponse callbackResponse = SuccessfulUpdateResponse.builder()
            .caseUpdateDetails(
                CaseCreationDetails
                    .builder()
                    .caseData(request.getCaseData())
                    .caseTypeId(CASE_TYPE_ID)
                    .eventId(UPDATE_EVENT_ID)
                    .build()
            ).warnings(Collections.emptyList())
            .build();

        return ResponseEntity.ok(callbackResponse);
    }
}
