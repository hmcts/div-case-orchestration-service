package uk.gov.hmcts.reform.divorce.orchestration.client;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.validation.PBAOrganisationResponse;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;

@FeignClient(name = "pba-validation-client", url = "${pba.validation.service.api.baseurl}")
public interface PBAValidationClient {

    @ApiOperation("Validates Solicitor Pay By Account (PBA) number for payment")
    @GetMapping(value = "/refdata/external/v1/organisations/pbas")
    ResponseEntity<PBAOrganisationResponse> retrievePBANumbers(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION_HEADER) String serviceAuthorisation,
        @RequestParam(name = "email") String email);
}
