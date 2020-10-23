package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class FeesAndPaymentHelper {

    public static final String APPLICATION_WITHOUT_NOTICE_FEE_URL = "/fees-and-payments/version/1/application-without-notice-fee";

    public static final String APPLICATION_WITHOUT_NOTICE = "Application (without notice)";
    public static final Integer AMOUNT = 50;
    public static final String AMOUNT_IN_PENNIES = String.valueOf(AMOUNT * 100);
    public static final String FEE_CODE = "FEE0228";

    public static void stubGetFeeFromFeesAndPayments(
        WireMockClassRule feesAndPaymentsServer, FeeResponse feeResponse) {
        feesAndPaymentsServer.stubFor(WireMock.get(APPLICATION_WITHOUT_NOTICE_FEE_URL)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(feeResponse))));
    }

    public static FeeResponse getApplicationWithoutNoticeFee() {
        return FeeResponse.builder()
            .amount(AMOUNT * 1d)
            .description(APPLICATION_WITHOUT_NOTICE)
            .feeCode(FEE_CODE)
            .version(1)
            .build();
    }

    public static CcdCallbackResponse buildExpectedResponse(FeeResponse applicationWithoutNoticeFee, String field) {
        Map<String, Object> expectedCaseData = new HashMap<>();

        OrderSummary orderSummary = getOrderSummary(applicationWithoutNoticeFee);

        expectedCaseData.put(field, orderSummary);

        return CcdCallbackResponse.builder()
            .data(expectedCaseData)
            .build();
    }

    public static OrderSummary getOrderSummary(FeeResponse feeResponse) {
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);

        return orderSummary;
    }
}
