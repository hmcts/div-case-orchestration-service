package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.prd.OrganisationsResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "organisation-client", url = "${prd.api.url}")
public interface OrganisationClient {

    @DeleteMapping(
        value = "/refdata/external/v1/organisations",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    OrganisationsResponse getMyOrganisation(
        @RequestHeader(AUTHORIZATION) String authorisation
    );
}
