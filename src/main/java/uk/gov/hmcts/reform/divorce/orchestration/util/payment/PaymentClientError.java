package uk.gov.hmcts.reform.divorce.orchestration.util.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.StatusHistoriesItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PaymentClientError {

    @Value("${pba.contact.phoneNumber}")
    private static String pbaContactPhoneNumber;

    @Value("${pba.contact.email}")
    private static String pbaContactEmail;

    private static final String CONTACT_INFO = " For Payment Account support call %s (Option 3) or email %s.";
    private static final String DEFAULT = "Payment request failed. Please use a different account or payment method.";

    private static final String CAE0001 = "CA-E0001";
    private static final String CAE0001_CONTENT = "Fee account %s has insufficient funds available.";

    private static final String CAE0004 = "CA-E0004";
    private static final String CAE0004_CONTENT = "Payment Account %s has been deleted or is on hold.";

    private static final String NOT_FOUND_CONTENT = "Payment Account %s cannot be found. "
        + "Please use a different account or payment method.";

    public static String getDefaultErrorMessage() {
        return DEFAULT + getContactInfo();
    }

    public static String getErrorMessage(HttpStatus httpStatus, CreditAccountPaymentResponse paymentResponse) {
        return getErrorMessage(httpStatus.value(), paymentResponse);
    }

    public static String getErrorMessage(int httpStatus, CreditAccountPaymentResponse paymentResponse) {

        log.error("Payment reference: \"{}\". Payment client failed with status: \"{}\".",
            paymentResponse.getReference(),
            paymentResponse.getStatus());

        return Optional.of(paymentResponse)
            .map(response -> {
                List<StatusHistoriesItem> statusHistories = Optional.ofNullable(response.getStatusHistories())
                    .orElseGet(ArrayList::new);
                String reference = response.getReference();

                if (httpStatus == HttpStatus.FORBIDDEN.value()) {
                    return getCustomForbiddenMessage(statusHistories, reference);
                } else if (httpStatus == HttpStatus.NOT_FOUND.value()) {
                    return getCustomErrorMessage(format(NOT_FOUND_CONTENT, reference));
                } else {
                    log.info("Returning default {} error message.", httpStatus);
                }

                return getCustomErrorMessage(DEFAULT);
            })
            .orElseGet(() -> getCustomErrorMessage(DEFAULT));
    }

    public static CreditAccountPaymentResponse getPaymentResponse(FeignException exception) {
        ObjectMapper objectMapper = new ObjectMapper();
        CreditAccountPaymentResponse creditAccountPaymentResponse = CreditAccountPaymentResponse.builder().build();
        try {
            creditAccountPaymentResponse = objectMapper.readValue(exception.contentUTF8(), CreditAccountPaymentResponse.class);
        } catch (JsonProcessingException jsonProcessingException) {
            log.warn("Could not convert error response to CreditAccountPaymentResponse object: {}", jsonProcessingException.getMessage());
        }
        return creditAccountPaymentResponse;
    }

    private static String getCustomForbiddenMessage(List<StatusHistoriesItem> statusHistories, String reference) {
        if (!statusHistories.isEmpty()) {
            StatusHistoriesItem statusHistoriesItem = statusHistories.get(0);
            String errorCode = Optional.ofNullable(statusHistoriesItem.getErrorCode()).orElseGet(() -> "");

            if (errorCode.equalsIgnoreCase(CAE0004)) {
                log.info("Payment Reference: {} Generating error message for {} error code:", reference, CAE0004);
                return getCustomErrorMessage(format(CAE0004_CONTENT, reference));

            } else if (errorCode.equalsIgnoreCase(CAE0001)) {
                log.info("Payment Reference: {} Generating error message for {} error code:", reference, CAE0001);
                return getCustomErrorMessage(format(CAE0001_CONTENT, reference));
            }
        }
        return getCustomErrorMessage(DEFAULT);
    }

    private static String getCustomErrorMessage(String value) {
        return value + getContactInfo();
    }

    private static String getContactInfo() {
        return format(CONTACT_INFO, pbaContactPhoneNumber, pbaContactEmail);
    }

}
