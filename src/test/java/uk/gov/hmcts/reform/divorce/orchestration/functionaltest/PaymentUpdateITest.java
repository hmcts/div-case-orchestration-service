package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.reform.divorce.model.payment.Payment;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Fee;
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
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class PaymentUpdateITest extends IdamTestSupport {

    private static final String CASE_ID = "1234567890";
    private static final String EVENT_ID = "paymentMade";

    private static final String API_URL = "/payment-update";

    private static final String ALLOWED_SERVICE = "test_service_allowed";
    private static final String NOT_ALLOWED_SERVICE = "test_service_not_allowed";

    private static final String RETRIEVE_CASE_CONTEXT_PATH = String.format(
            "/casemaintenance/version/1/case/%s",
            CASE_ID
    );
    private static final String UPDATE_CONTEXT_PATH = String.format(
            "/casemaintenance/version/1/updateCase/%s/%s",
            CASE_ID,
            EVENT_ID
    );
    private static final String AUTH_SERVICE_PATH = "/details";

    @Autowired
    private MockMvc webClient;

    private final PaymentUpdate paymentUpdate = new PaymentUpdate();
    private Payment payment = Payment.builder().build();
    private final Map<String, Object> caseData = new HashMap<>();

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

        stubSignInForCaseworker();
        stubMaintenanceServerEndpointForRetrieveCaseById();

        Map<String, Object> responseData = Collections.singletonMap(ID, TEST_CASE_ID);
        stubMaintenanceServerEndpointForUpdate(responseData);

        stubAuthProviderServerEndpoint();
        stubForbiddenAuthProviderServerEndpoint();
    }

    @Test
    public void givenEventDataAndAuth_whenEventDataIsSubmitted_thenReturnSuccess() throws Exception {
        webClient.perform(put(API_URL)
                    .header(SERVICE_AUTHORIZATION_HEADER, BEARER_AUTH_TOKEN_1)
                    .content(convertObjectToJsonString(paymentUpdate))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void givenEventDataAndForbiddenAuth_whenEventDataIsSubmitted_thenReturnError() throws Exception {
        webClient.perform(put(API_URL)
            .header(SERVICE_AUTHORIZATION_HEADER, BEARER_AUTH_TOKEN)
            .content(convertObjectToJsonString(paymentUpdate))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void givenEventDataAndNoAuth_whenEventDataIsSubmitted_thenReturnError() throws Exception {
        webClient.perform(put(API_URL)
            .header(SERVICE_AUTHORIZATION_HEADER, "")
            .content(convertObjectToJsonString(paymentUpdate))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is4xxClientError());
    }

    private void stubMaintenanceServerEndpointForRetrieveCaseById() {
        maintenanceServiceServer.stubFor(WireMock.get(RETRIEVE_CASE_CONTEXT_PATH)
                .withHeader(AUTHORIZATION, new EqualToPattern(BEARER_AUTH_TOKEN_1))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(convertObjectToJsonString(caseData))));
    }

    private void stubMaintenanceServerEndpointForUpdate(Map<String, Object> response) {
        maintenanceServiceServer.stubFor(WireMock.post(UPDATE_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
                .withHeader(AUTHORIZATION, new EqualToPattern(BEARER_AUTH_TOKEN_1))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(convertObjectToJsonString(response))));
    }

    private void stubAuthProviderServerEndpoint() {
        serviceAuthProviderServer.stubFor(WireMock.get(AUTH_SERVICE_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(BEARER_AUTH_TOKEN_1))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(ALLOWED_SERVICE)));
    }

    private void stubForbiddenAuthProviderServerEndpoint() {
        serviceAuthProviderServer.stubFor(WireMock.get(AUTH_SERVICE_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(BEARER_AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(NOT_ALLOWED_SERVICE)));
    }
}
