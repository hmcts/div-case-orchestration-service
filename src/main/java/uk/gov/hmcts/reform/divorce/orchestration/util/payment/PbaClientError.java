package uk.gov.hmcts.reform.divorce.orchestration.util.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.StatusHistoriesItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PbaClientError {

    private static final String CAE0001 = "CA-E0001";
    private static final String CAE0004 = "CA-E0004";

    public static String getDefaultErrorMessage() {
        return PbaErrorMessage.GENERAL.value();
    }

    public static String getErrorMessage(HttpStatus httpStatus, CreditAccountPaymentResponse paymentResponse) {
        return getErrorMessage(httpStatus.value(), paymentResponse);
    }

    public static String getErrorMessage(int httpStatus, CreditAccountPaymentResponse paymentResponse) {
        return Optional.of(paymentResponse)
            .map(response -> processErrorMessage(httpStatus, response))
            .orElseGet(PbaClientError::getDefaultErrorMessage);
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

    private static String processErrorMessage(int httpStatus, CreditAccountPaymentResponse paymentResponse) {
        log.error("Payment reference: \"{}\". Payment client failed with status: \"{}\".",
            paymentResponse.getReference(),
            paymentResponse.getStatus());

        List<StatusHistoriesItem> statusHistories = getStatusHistories(paymentResponse);
        String paymentReference = getPaymentReference(paymentResponse);

        if (httpStatus == HttpStatus.FORBIDDEN.value()) {
            return getCustomForbiddenMessage(statusHistories, paymentReference);
        }
        if (httpStatus == HttpStatus.NOT_FOUND.value()) {
            return formatContent(paymentReference, PbaErrorMessage.NOTFOUND.value());
        }

        log.info("Returning default {} error message.", httpStatus);
        return getDefaultErrorMessage();
    }

    private static String getCustomForbiddenMessage(List<StatusHistoriesItem> statusHistories, String paymentReference) {
        if (!statusHistories.isEmpty()) {
            StatusHistoriesItem statusHistoriesItem = statusHistories.get(0);
            String errorCode = getErrorCode(statusHistoriesItem);

            if (errorCode.equalsIgnoreCase(CAE0004)) {
                log.info("Payment Reference: {} Generating error message for {} error code:", paymentReference, CAE0004);
                return formatContent(paymentReference, PbaErrorMessage.CAE0004.value());
            }
            if (errorCode.equalsIgnoreCase(CAE0001)) {
                log.info("Payment Reference: {} Generating error message for {} error code:", paymentReference, CAE0001);
                return formatContent(paymentReference, PbaErrorMessage.CAE0001.value());
            }
        } else {
            log.info("Payment Reference: {} Status histories is empty. Cannot process custom message for this error", paymentReference);
        }

        return getDefaultErrorMessage();
    }

    private static String formatContent(String paymentReference, String content) {
        return format(content, paymentReference);
    }

    private static String getErrorCode(StatusHistoriesItem statusHistoriesItem) {
        return Optional.ofNullable(statusHistoriesItem.getErrorCode())
            .orElseGet(() -> StringUtils.EMPTY);
    }

    private static String getPaymentReference(CreditAccountPaymentResponse response) {
        return Optional.ofNullable(response.getReference())
            .orElse(StringUtils.EMPTY);
    }

    private static List<StatusHistoriesItem> getStatusHistories(CreditAccountPaymentResponse response) {
        return Optional.ofNullable(response.getStatusHistories())
            .orElseGet(ArrayList::new);
    }
}
