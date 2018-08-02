package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "validation-service-client", url = "${case.validation.service.api.baseurl}")
public interface CaseValidationClient {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/version/1/validate",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    ValidationResponse validate(
        @RequestBody ValidationRequest validationRequest);
}

