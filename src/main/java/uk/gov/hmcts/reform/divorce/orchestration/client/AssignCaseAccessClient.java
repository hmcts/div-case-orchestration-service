package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AssignCaseAccessRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.DocumentContentFetcherService.Headers.SERVICE_AUTHORIZATION;

@FeignClient(name = "aca-api-client", url = "${aca.api.url}")
public interface AssignCaseAccessClient {

    @PostMapping(
        value = "/case-assignments",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void assignCaseAccess(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody final AssignCaseAccessRequest assignCaseAccessRequest
    );
}
