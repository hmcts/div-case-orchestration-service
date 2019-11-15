package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.out.CaseCreationDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.out.SuccessfulTransformationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.OcrValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations.D8FormToCaseTransformer;

import java.util.Collections;
import java.util.Map;
import javax.validation.Valid;

import static java.util.Collections.emptyList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.out.ValidationStatus.SUCCESS;

@Slf4j
@Controller
public class BulkScanController {

    private static final String CASE_TYPE_ID = "DIVORCE";
    private static final String EVENT_ID = "EVENT_ID";

    D8FormToCaseTransformer d8FormToCaseTransformer = new D8FormToCaseTransformer();

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
        @RequestHeader(name = "ServiceAuthorization", required = false) String serviceAuthHeader,
        @PathVariable(name = "form-type", required = false) String formType,
        @Valid @RequestBody OcrDataValidationRequest request
    ) {
        log.info("Validating form {} for bulk scanning operation", formType);

        return ok().body(new OcrValidationResponse(emptyList(), emptyList(), SUCCESS));
    }

    @PostMapping(
            path = "/transform-exception-record",
            consumes = APPLICATION_JSON,
            produces = APPLICATION_JSON
    )
    @ApiOperation(value = "Transform exception record into CCD case data")
    @ApiResponses({
            @ApiResponse(code = 200, response = SuccessfulTransformationResponse.class,
                    message = "Transformation of exception record into case data has been successful"
            ),
            @ApiResponse(code = 400, message = "Request failed due to malformed syntax (and only for that reason)"),
            @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
            @ApiResponse(code = 403, message = "Calling service is not authorised to use the endpoint"),
            @ApiResponse(code = 422, message = "Exception record is well-formed, but contains invalid data.")
    })
    public ResponseEntity<SuccessfulTransformationResponse> transformExceptionRecordToCase(
            @RequestHeader(name = "ServiceAuthorization", required = false) String serviceAuthHeader,
            @Valid @RequestBody ExceptionRecord exceptionRecord
    ) {
        log.info("Transforming exception record to case");

        Map<String, Object> transformedCaseData = d8FormToCaseTransformer.transformIntoCaseData(exceptionRecord);

        SuccessfulTransformationResponse callbackResponse = SuccessfulTransformationResponse.builder()
                .caseCreationDetails(
                        new CaseCreationDetails(
                                CASE_TYPE_ID,
                                EVENT_ID,
                                transformedCaseData))
                .warnings(Collections.emptyList())
                .build();

        return ResponseEntity.ok(callbackResponse);
    }
}