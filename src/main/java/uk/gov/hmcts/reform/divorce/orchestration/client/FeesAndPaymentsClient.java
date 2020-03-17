package uk.gov.hmcts.reform.divorce.orchestration.client;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "fees-and-payments-client", url = "${fees-and-payments.service.api.baseurl}")
public interface FeesAndPaymentsClient {

    @ApiOperation("Returns Petition Issue Fee")
    @GetMapping(value = "/fees-and-payments/version/1/petition-issue-fee",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    FeeResponse getPetitionIssueFee();

    @ApiOperation("Returns amend Petitioner Fee")
    @GetMapping(value = "/fees-and-payments/version/1/amend-fee",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    FeeResponse getAmendPetitioneFee();
}