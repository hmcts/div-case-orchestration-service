package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.GetGeneralApplicationWithoutNoticeFeeTask.GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class FeeLookupTest extends IdamTestSupport {

    private static final String API_URL = "/fee-lookup";
    private static final String APPLICATION_WITHOUT_NOTICE_FEE_URL = "/fees-and-payments/version/1/application-without-notice-fee";
    public static final FeeResponse applicationWithoutNoticeFee = FeeResponse.builder()
        .amount(50d)
        .description("Application (without notice)")
        .feeCode("FEE0228")
        .version(1)
        .build();

    @Autowired
    private MockMvc webClient;

    @Test
    public void shouldPopulateGeneralApplicationWithoutNoticeFeeSummaryInResponse() throws Exception {
        FeeResponse applicationWithoutNoticeFee = FeeResponse.builder()
            .amount(50d)
            .description("Application (without notice)")
            .feeCode("FEE0228")
            .version(1)
            .build();
        CcdCallbackRequest input = buildRequest();
        CcdCallbackResponse expectedResponse = buildExpectedResponse(applicationWithoutNoticeFee);

        stubGetFeeFromFeesAndPayments(applicationWithoutNoticeFee);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    private void stubGetFeeFromFeesAndPayments(FeeResponse feeResponse) {
        feesAndPaymentsServer.stubFor(WireMock.get(APPLICATION_WITHOUT_NOTICE_FEE_URL)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(feeResponse))));
    }

    private CcdCallbackResponse buildExpectedResponse(FeeResponse applicationWithoutNoticeFee) {
        Map<String, Object> expectedCaseData = new HashMap<>();

        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(applicationWithoutNoticeFee);

        expectedCaseData.put(
            GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY,
            orderSummary
        );

        return CcdCallbackResponse.builder()
            .data(expectedCaseData)
            .build();
    }

    private CcdCallbackRequest buildRequest() {
        return new CcdCallbackRequest(
            AUTH_TOKEN,
            "",
            CaseDetails.builder().caseData(new HashMap<>()).caseId(TEST_CASE_ID).build()
        );
    }
}
