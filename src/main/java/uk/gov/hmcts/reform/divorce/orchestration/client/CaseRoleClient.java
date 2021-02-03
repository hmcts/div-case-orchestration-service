package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.request.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.request.UpdateUserRolesRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.DocumentContentFetcherService.Headers.SERVICE_AUTHORIZATION;

@FeignClient(name = "case-role-client", url = "${ccd.data-store.api.url}")
public interface CaseRoleClient {

    @DeleteMapping(
        value = "/case-users",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void removeCaseRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody final RemoveUserRolesRequest request
    );

    @PutMapping("/cases/{caseReference}/users/{userId}")
    void updateCaseRolesForUser(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("caseReference") String caseReference,
        @PathVariable("userId") String userId,
        @RequestBody final UpdateUserRolesRequest request
    );
}
