package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.StatusHistoriesItem;
import uk.gov.hmcts.reform.divorce.orchestration.util.payment.PbaErrorMessage;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public class PbaClientErrorTestUtil {
    public static final String TEST_REFERENCE = "RC-1601-8933-5348-8116";
    public static final String TEST_ERROR_MESSAGE = "error message";

    public static CreditAccountPaymentResponse getBasicFailedResponse() {
        return buildPaymentClientResponse("Failed", null);
    }

    public static CreditAccountPaymentResponse buildPaymentClientResponse(String status, String errorCode) {
        return CreditAccountPaymentResponse.builder()
            .reference(TEST_REFERENCE)
            .dateCreated("2020-10-05T10:22:33.449+0000")
            .status(status)
            .paymentGroupReference("2020-1601893353478")
            .statusHistories(
                asList(
                    StatusHistoriesItem.builder()
                        .status(status.toLowerCase())
                        .errorCode(errorCode)
                        .errorMessage(TEST_ERROR_MESSAGE)
                        .dateCreated("2020-10-05T10:22:33.458+0000")
                        .dateUpdated("2020-10-05T10:22:33.458+0000")
                        .build()))
            .build();
    }

    public static String formatMessage(PbaErrorMessage pbaErrorMessage) {
        return format(pbaErrorMessage.value(), TEST_REFERENCE);
    }

    public static FeignException buildException(HttpStatus httpStatus, CreditAccountPaymentResponse paymentResponse) {
        byte[] body = ObjectMapperTestUtil.convertObjectToJsonString(paymentResponse).getBytes();
        return new FeignException.FeignClientException(httpStatus.value(), TEST_ERROR_MESSAGE, body);
    }
}
