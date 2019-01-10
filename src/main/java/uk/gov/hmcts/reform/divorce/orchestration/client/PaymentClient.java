package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Payment;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;

@FeignClient(name = "payment-client", url = "${payment.service.api.baseurl}")
public interface PaymentClient {

    @RequestMapping(method = RequestMethod.POST, value = "/credit-account-payments")
    ResponseEntity<CreditAccountPaymentResponse> creditAccountPayment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION_HEADER) String serviceAuthorisation,
            CreditAccountPaymentRequest creditAccountPaymentRequest);

    @RequestMapping(method = RequestMethod.GET, value = "/card-payments/{paymentRef}")
    Map<String, Object> checkPayment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION_HEADER) String serviceAuthorisation,
            @PathVariable("paymentRef") String paymentRef);
}