package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;
import uk.gov.hmcts.reform.divorce.orchestration.event.listener.DataExtractionRequestListener;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DN;

/**
 * This class provides endpoint to trigger the data extraction process on demand.
 */
@RestController
@RequiredArgsConstructor
public class DataExtractionController {

    @Autowired
    private final DataExtractionRequestListener listener;

    @PostMapping(path = "/cases/data-extraction/family-man")
    @ApiOperation(value = "Starts data extraction for FamilyMan for the day before today. This is meant to only be used as a testing tool.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Data Extraction Process Started"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public void startDataExtractionToFamilyMan() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        listener.onApplicationEvent(new DataExtractionRequest(this, DA, yesterday));
        listener.onApplicationEvent(new DataExtractionRequest(this, AOS, yesterday));
        listener.onApplicationEvent(new DataExtractionRequest(this, DN, yesterday));
    }

    @PostMapping(path = "/cases/data-extraction/family-man/status/{status}/lastModifiedDate/{lastModifiedDate}")
    @ApiOperation(value = "Starts data extraction for FamilyMan for given status and given date.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Data extraction process started"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public void startDataExtractionToFamilyManForGivenStatusAndDate(
        @PathVariable(value = "status")
            DataExtractionRequest.Status status,
        @PathVariable(value = "lastModifiedDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate lastModifiedDate) {
        listener.onApplicationEvent(new DataExtractionRequest(this, status, lastModifiedDate));
    }
}