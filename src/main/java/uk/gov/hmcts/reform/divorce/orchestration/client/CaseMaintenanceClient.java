package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@FeignClient(name = "maintenance-service-client", url = "${case.maintenance.service.api.baseurl}")
public interface CaseMaintenanceClient {

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/casemaintenance/version/1/submit",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> submitCase(
            @RequestBody Map<String, Object> submitCase,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);

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
    Map<String, Object> retrievePetition(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
            @RequestParam(value = "checkCcd") boolean checkCcd);

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
}