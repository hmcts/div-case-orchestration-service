package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "fees-and-payments-client", url = "${fees-and-payments.service.api.baseurl}")
public interface FeesAndPaymentsClient {

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/fees-and-payments/version/1/petition-issue-fee",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    FeeResponse getPetitionIssueFee();
}