package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
            method = RequestMethod.POST,
            value = "/casemaintenance/version/1/updateCase/{caseId}/{eventId}",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> updateCase(
        @RequestBody Map<String, Object> updateCase,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @PathVariable String caseId,
        @PathVariable String eventId);
}