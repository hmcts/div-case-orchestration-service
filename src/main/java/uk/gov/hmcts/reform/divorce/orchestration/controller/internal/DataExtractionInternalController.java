package uk.gov.hmcts.reform.divorce.orchestration.controller.internal;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;

/**
 * This class provides endpoint so trigger the data extraction process on demand (meant to be used for tests).
 */
@RestController
@RequiredArgsConstructor
public class DataExtractionInternalController {

    @Autowired
    private final DataExtractionService dataExtractionService;

    @PostMapping(path = "/cases/data-extraction/family-man")
    @ApiOperation(value = "Starts data extraction for family man for the present day. This is meant to only be used as a testing tool.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Data extraction process started"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public void startDataExtractionToFamilyMan() throws CaseOrchestrationServiceException {
        dataExtractionService.requestDataExtractionForPreviousDay();
    }
}
