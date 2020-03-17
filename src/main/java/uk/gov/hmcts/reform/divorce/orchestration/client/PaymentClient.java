package uk.gov.hmcts.reform.divorce.orchestration.client;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;

@FeignClient(name = "payment-client", url = "${payment.service.api.baseurl}")
public interface PaymentClient {

    @ApiOperation("Handles Solicitor Payment By Account (PBA) Payments")
    @PostMapping(value = "/credit-account-payments")
    ResponseEntity<CreditAccountPaymentResponse> creditAccountPayment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION_HEADER) String serviceAuthorisation,
            CreditAccountPaymentRequest creditAccountPaymentRequest);

    @ApiOperation("Returns payment information for given payment reference")
    @GetMapping(value = "/card-payments/{paymentRef}")
    Map<String, Object> checkPayment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION_HEADER) String serviceAuthorisation,
            @PathVariable("paymentRef") String paymentRef);
}
