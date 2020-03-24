package uk.gov.hmcts.reform.divorce.orchestration.client;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "maintenance-service-client", url = "${case.maintenance.service.api.baseurl}")
public interface CaseMaintenanceClient {

    @ApiOperation("Amend Petition")
    @PutMapping(value = "/casemaintenance/version/1/amended-petition-draft",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> amendPetition(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken
    );

    @ApiOperation("Amend Petition For Refusal")
    @PutMapping(value = "/casemaintenance/version/1/amended-petition-draft-refusal",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> amendPetitionForRefusal(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken
    );

    @ApiOperation("Submit Case")
    @PostMapping(value = "/casemaintenance/version/1/submit",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> submitCase(
        @RequestBody Map<String, Object> submitCase,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken
    );

    @ApiOperation("Update Case")
    @PostMapping(value = "/casemaintenance/version/1/updateCase/{caseId}/{eventId}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> updateCase(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @PathVariable("caseId") String caseId,
        @PathVariable("eventId") String eventId,
        @RequestBody Map<String, Object> requestBody
    );

    @ApiOperation("Retrieve AOS Case")
    @GetMapping(value = "/casemaintenance/version/1/retrieveAosCase",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    CaseDetails retrieveAosCase(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken
    );

    @ApiOperation("Link Respondent with Case ID and Letter Holder ID")
    @PostMapping(value = "/casemaintenance/version/1/link-respondent/{caseId}/{letterHolderId}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    void linkRespondent(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @PathVariable("caseId") String caseId,
        @PathVariable("letterHolderId") String letterHolderId);

    @ApiOperation("Unlink Respondent from case")
    @DeleteMapping(value = "/casemaintenance/version/1/link-respondent/{caseId}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    void unlinkRespondent(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @PathVariable("caseId") String caseId);

    @ApiOperation("Save draft case")
    @PutMapping(value = "/casemaintenance/version/1/drafts",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> saveDraft(
            @RequestBody Map<String, Object> draft,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @RequestParam(value = "divorceFormat") boolean divorceFormat);

    @ApiOperation("Retrieve Petition")
    @GetMapping(value = "/casemaintenance/version/1/retrieveCase")
    CaseDetails retrievePetition(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);

    @ApiOperation("Get Case")
    @GetMapping(value = "/casemaintenance/version/1/case")
    CaseDetails getCase(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);

    @ApiOperation("Retrieve Petition by ID")
    @GetMapping(value = "/casemaintenance/version/1/case/{caseId}")
    CaseDetails retrievePetitionById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @PathVariable("caseId") String caseId);

    @ApiOperation("Delete draft case")
    @DeleteMapping(value = "/casemaintenance/version/1/drafts")
    Map<String, Object> deleteDraft(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);

    @ApiOperation("Get drafts")
    @GetMapping(value = "/casemaintenance/version/1/drafts")
    Map<String, Object> getDrafts(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);

    @ApiOperation("Search cases")
    @PostMapping(value = "/casemaintenance/version/1/search",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    SearchResult searchCases(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @RequestBody String query
    );

    @ApiOperation("Submit bulk case")
    @PostMapping(value = "/casemaintenance/version/1/bulk/submit",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> submitBulkCase(
        @RequestBody Map<String, Object> bulkCase,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken
    );

    @ApiOperation("Update bulk case")
    @PostMapping(value = "/casemaintenance/version/1/bulk/updateCase/{caseId}/{eventId}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> updateBulkCase(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @PathVariable("caseId") String caseId,
            @PathVariable("eventId") String eventId,
            @RequestBody Map<String, Object> requestBody
    );

    @ApiOperation("Add Petitioner Solicitor Role to case")
    @PutMapping(value = "/casemaintenance/version/1/add-petitioner-solicitor-role/{caseId}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    void addPetitionerSolicitorRole(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @PathVariable("caseId") String caseId
    );
}