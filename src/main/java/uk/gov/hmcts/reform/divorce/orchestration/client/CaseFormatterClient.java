package uk.gov.hmcts.reform.divorce.orchestration.client;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "formatter-service-client", url = "${case.formatter.service.api.baseurl}")
public interface CaseFormatterClient {

    @ApiOperation("Add documents to case")
    @PostMapping(value = "/caseformatter/version/1/add-documents",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> addDocuments(
        @RequestBody DocumentUpdateRequest documentUpdateRequest
    );

    @ApiOperation("Remove all Petition documents from case data")
    @PostMapping(value = "/caseformatter/version/1/remove-all-petition-documents",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> removeAllPetitionDocuments(
        @RequestBody Map<String, Object> caseData
    );

    @ApiOperation("Remove all documents by document type")
    @PostMapping(value = "/caseformatter/version/1/remove/documents/{documentType}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> removeAllDocumentsByType(
        @PathVariable("documentType") String eventId,
        @RequestBody Map<String, Object> caseData
    );

    @ApiOperation("Transform data from Divorce format to CCD format")
    @PostMapping(value = "/caseformatter/version/1/to-ccd-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> transformToCCDFormat(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @RequestBody Map<String, Object> transformToCCDFormat
    );

    @ApiOperation("Transform data to Divorce format")
    @PostMapping(value = "/caseformatter/version/1/to-divorce-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> transformToDivorceFormat(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @RequestBody Map<String, Object> transformToDivorceFormat
    );

    @ApiOperation("Transform data to AOS Case Format")
    @PostMapping(value = "/caseformatter/version/1/to-aos-submit-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> transformToAosCaseFormat(
        @RequestBody Map<String, Object> divorceSession
    );

    @ApiOperation("Transform data to DN Submit Format")
    @PostMapping(value = "/caseformatter/version/1/to-dn-submit-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> transformToDnCaseFormat(
            @RequestBody Map<String, Object> divorceSession
    );

    @ApiOperation("Transform data to DA Submit Format")
    @PostMapping(value = "/caseformatter/version/1/to-da-submit-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> transformToDaCaseFormat(
        @RequestBody Map<String, Object> divorceSession
    );

    @ApiOperation("Transform data to DN Clarification Format")
    @PostMapping(value = "/caseformatter/version/1/to-dn-clarification-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> transformToDnClarificationCaseFormat(
        @RequestBody Map<String, Object> divorceCaseWrapper
    );
}
