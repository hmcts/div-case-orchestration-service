package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.StatusHistoriesItem;

import static java.util.Arrays.asList;

public class PbaClientErrorTestUtil {
    public static final String TEST_REFERENCE = "RC-1601-8933-5348-8116";

    public static CreditAccountPaymentResponse getBasicFailedResponse() {
        return buildPaymentClientResponse("Failed", null, null);
    }

    public static CreditAccountPaymentResponse buildPaymentClientResponse(String status, String errorCode, String errorMessage) {
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
                        .errorMessage(errorMessage)
                        .dateCreated("2020-10-05T10:22:33.458+0000")
                        .dateUpdated("2020-10-05T10:22:33.458+0000")
                        .build()))
            .build();
    }
}
