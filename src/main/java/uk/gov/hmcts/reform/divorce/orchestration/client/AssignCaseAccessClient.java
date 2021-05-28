package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AssignCaseAccessRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.orchestration.config.QueryParams.USE_USER_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.config.SpecificHttpHeaders.SERVICE_AUTHORIZATION;

@FeignClient(name = "aca-api-client", url = "${aca.api.url}")
public interface AssignCaseAccessClient {

    @PostMapping(
        value = "/case-assignments",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void assignCaseAccess(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam(USE_USER_TOKEN) boolean useUserToken,
        @RequestBody final AssignCaseAccessRequest assignCaseAccessRequest
    );
}
