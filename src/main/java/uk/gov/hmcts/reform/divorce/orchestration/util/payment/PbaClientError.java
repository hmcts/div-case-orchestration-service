package uk.gov.hmcts.reform.divorce.orchestration.util.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.StatusHistoriesItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PbaClientError {

    private static final String CAE0001 = "CA-E0001";
    private static final String CAE0004 = "CA-E0004";

    public static String getMessage(String pbaNumber, FeignException exception) {
        return getErrorMessage(getHttpStatus(exception), getPaymentResponse(exception), pbaNumber);
    }

    private static String getErrorMessage(HttpStatus httpStatus, CreditAccountPaymentResponse paymentResponse, String pbaNumber) {
        return Optional.of(paymentResponse)
            .map(response -> processErrorMessage(httpStatus, response, pbaNumber))
            .orElseGet(PbaClientError::getDefaultErrorMessage);
    }

    private static CreditAccountPaymentResponse getPaymentResponse(FeignException exception) {
        ObjectMapper objectMapper = new ObjectMapper();
        CreditAccountPaymentResponse creditAccountPaymentResponse = CreditAccountPaymentResponse.builder().build();
        try {
            creditAccountPaymentResponse = objectMapper.readValue(exception.contentUTF8(), CreditAccountPaymentResponse.class);
        } catch (JsonProcessingException jsonProcessingException) {
            log.warn("Could not convert error response to CreditAccountPaymentResponse object. Error message was {}",
                exception.contentUTF8());
            log.error(jsonProcessingException.getMessage());
        }
        return creditAccountPaymentResponse;
    }

    private static String getDefaultErrorMessage() {
        return PbaErrorMessage.GENERAL.value();
    }

    private static HttpStatus getHttpStatus(FeignException exception) {
        return Optional.ofNullable(HttpStatus.resolve(exception.status()))
            .orElseGet(() -> HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String processErrorMessage(HttpStatus httpStatus, CreditAccountPaymentResponse paymentResponse, String pbaNumber) {
        log.error("PBA account {}. Payment client failed with status: \"{}\".",
            pbaNumber,
            paymentResponse.getStatus());

        List<StatusHistoriesItem> statusHistories = getStatusHistories(paymentResponse);

        if (httpStatus == HttpStatus.FORBIDDEN) {
            return getCustomForbiddenMessage(statusHistories, pbaNumber);
        }
        if (httpStatus == HttpStatus.NOT_FOUND) {
            return formatContent(pbaNumber, PbaErrorMessage.NOTFOUND.value());
        }

        log.info("Returning default {} error message.", httpStatus);
        return getDefaultErrorMessage();
    }

    private static String getCustomForbiddenMessage(List<StatusHistoriesItem> statusHistories, String pbaNumber) {
        if (!statusHistories.isEmpty()) {
            StatusHistoriesItem statusHistoriesItem = statusHistories.get(0);
            String errorCode = getErrorCode(statusHistoriesItem);

            if (errorCode.equalsIgnoreCase(CAE0001)) {
                log.info("Payment Reference: {} Generating error message for {} error code:", pbaNumber, CAE0001);
                return formatContent(pbaNumber, PbaErrorMessage.CAE0001.value());
            }
            if (errorCode.equalsIgnoreCase(CAE0004)) {
                log.info("Payment Reference: {} Generating error message for {} error code:", pbaNumber, CAE0004);
                return formatContent(pbaNumber, PbaErrorMessage.CAE0004.value());
            }
        } else {
            log.info("Payment Reference: {} Status histories is empty. Cannot process custom message for this error", pbaNumber);
        }

        return getDefaultErrorMessage();
    }

    private static String formatContent(String paymentReference, String content) {
        return format(content, paymentReference);
    }

    private static String getErrorCode(StatusHistoriesItem statusHistoriesItem) {
        return Optional.ofNullable(statusHistoriesItem.getErrorCode())
            .orElseGet(() -> EMPTY);
    }

    private static String getPaymentAccountNumber(CreditAccountPaymentResponse response) {
        return Optional.ofNullable(response.getAccountNumber())
            .orElseGet(() -> EMPTY);
    }

    private static List<StatusHistoriesItem> getStatusHistories(CreditAccountPaymentResponse response) {
        return Optional.ofNullable(response.getStatusHistories())
            .orElseGet(ArrayList::new);
    }
}
