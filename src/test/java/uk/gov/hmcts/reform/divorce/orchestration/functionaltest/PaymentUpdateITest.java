package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Fee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Payment;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class PaymentUpdateITest extends IdamTestSupport {

    private static final String CASE_ID = "1234567890";
    private static final String EVENT_ID = "paymentMade";

    private static final String API_URL = "/payment-update";

    private static final String RETRIEVE_CASE_CONTEXT_PATH = String.format(
            "/casemaintenance/version/1/case/%s",
            CASE_ID
    );
    private static final String CCD_FORMAT_CONTEXT_PATH = "/caseformatter/version/1/to-ccd-format";
    private static final String UPDATE_CONTEXT_PATH = String.format(
            "/casemaintenance/version/1/updateCase/%s/%s",
            CASE_ID,
            EVENT_ID
    );

    @Autowired
    private MockMvc webClient;

    private PaymentUpdate paymentUpdate = new PaymentUpdate();
    private Payment payment = Payment.builder().build();
    private Map<String, Object> caseData = new HashMap<>();

    @Before
    public void setup() {
        paymentUpdate.setChannel("online");
        paymentUpdate.setCcdCaseNumber(CASE_ID);
        paymentUpdate.setReference("paymentReference");
        paymentUpdate.setSiteId("siteId");
        paymentUpdate.setStatus("success");
        paymentUpdate.setExternalReference("externalReference");
        paymentUpdate.setAmount(new BigDecimal(550.00));

        Fee fee = new Fee();
        fee.setCode("X243");
        paymentUpdate.setFees(Arrays.asList(fee, fee));

        payment = Payment.builder()
            .paymentChannel("online")
            .paymentReference(paymentUpdate.getReference())
            .paymentSiteId(paymentUpdate.getSiteId())
            .paymentStatus(paymentUpdate.getStatus())
            .paymentTransactionId(paymentUpdate.getExternalReference())
            .paymentAmount("55000")
            .paymentFeeId("X243")
            .build();

        caseData.put("payment", payment);
    }

    @Test
    public void givenEventDataAndAuth_whenEventDataIsSubmitted_thenReturnSuccess() throws Exception {
        stubSignInForCaseworker();
        stubMaintenanceServerEndpointForRetrieveCaseById();
        stubFormatterServerEndpoint();

        Map<String, Object> responseData = Collections.singletonMap(ID, TEST_CASE_ID);
        stubMaintenanceServerEndpointForUpdate(responseData);

        webClient.perform(put(API_URL)
                .content(convertObjectToJsonString(paymentUpdate))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    private void stubMaintenanceServerEndpointForRetrieveCaseById() {
        maintenanceServiceServer.stubFor(WireMock.get(RETRIEVE_CASE_CONTEXT_PATH)
                .withHeader(AUTHORIZATION, new EqualToPattern(BEARER_AUTH_TOKEN_1))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(caseData))));
    }

    private void stubFormatterServerEndpoint() {
        formatterServiceServer.stubFor(WireMock.post(CCD_FORMAT_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(caseData))));
    }

    private void stubMaintenanceServerEndpointForUpdate(Map<String, Object> response) {
        maintenanceServiceServer.stubFor(WireMock.post(UPDATE_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
                .withHeader(AUTHORIZATION, new EqualToPattern(BEARER_AUTH_TOKEN_1))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(response))));
    }
}
