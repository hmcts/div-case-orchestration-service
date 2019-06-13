package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "maintenance-service-client", url = "${case.maintenance.service.api.baseurl}")
public interface CaseMaintenanceClient {

    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/casemaintenance/version/1/amended-petition-draft",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> amendPetition(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/casemaintenance/version/1/submit",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> submitCase(
        @RequestBody Map<String, Object> submitCase,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken
    );

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/casemaintenance/version/1/updateCase/{caseId}/{eventId}",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> updateCase(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @PathVariable("caseId") String caseId,
        @PathVariable("eventId") String eventId,
        @RequestBody Map<String, Object> requestBody
    );

    @RequestMapping(
        method = RequestMethod.GET,
        value = "/casemaintenance/version/1/retrieveAosCase",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    CaseDetails retrieveAosCase(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/casemaintenance/version/1/link-respondent/{caseId}/{letterHolderId}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    void linkRespondent(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @PathVariable("caseId") String caseId,
        @PathVariable("letterHolderId") String letterHolderId);

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/casemaintenance/version/1/link-respondent/{caseId}",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    void unlinkRespondent(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @PathVariable("caseId") String caseId);

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/casemaintenance/version/1/drafts",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> saveDraft(
            @RequestBody Map<String, Object> draft,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @RequestParam(value = "divorceFormat") boolean divorceFormat);

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/casemaintenance/version/1/retrieveCase"
    )
    CaseDetails retrievePetition(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);

    @RequestMapping(
        method = RequestMethod.GET,
        value = "/casemaintenance/version/1/case"
    )
    CaseDetails getCase(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/casemaintenance/version/1/case/{caseId}"
    )
    CaseDetails retrievePetitionById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @PathVariable("caseId") String caseId);

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/casemaintenance/version/1/drafts"
    )
    Map<String, Object> deleteDraft(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/casemaintenance/version/1/drafts"
    )
    Map<String, Object> getDrafts(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/casemaintenance/version/1/search",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    SearchResult searchCases(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @RequestBody String query
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/casemaintenance/version/1/bulk/submit",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> submitBulkCase(
        @RequestBody Map<String, Object> bulkCase,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken
    );

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/casemaintenance/version/1/bulk/updateCase/{caseId}/{eventId}",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> updateBulkCase(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @PathVariable("caseId") String caseId,
            @PathVariable("eventId") String eventId,
            @RequestBody Map<String, Object> requestBody
    );
}