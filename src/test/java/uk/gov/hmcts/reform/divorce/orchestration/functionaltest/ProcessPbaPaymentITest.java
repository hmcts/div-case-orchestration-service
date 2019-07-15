package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeValue;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CURRENCY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FIRM_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ProcessPbaPaymentITest {
    private static final String API_URL = "/process-pba-payment";
    private static final String PAYMENTS_CREDIT_ACCOUNT_CONTEXT_PATH = "/credit-account-payments";
    private static final String SERVICE_AUTH_CONTEXT_PATH = "/lease";
    private static final String FORMAT_REMOVE_PETITION_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/remove-all-petition-documents";
    private static final String YES_VALUE = "YES";
    private static final String NO_VALUE = "NO";

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule paymentsServer = new WireMockClassRule(9190);

    @ClassRule
    public static WireMockClassRule serviceAuthProviderServer = new WireMockClassRule(4504);

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    private Map<String, Object> caseData;
    private CaseDetails caseDetails;
    private CcdCallbackRequest ccdCallbackRequest;
    private CreditAccountPaymentRequest request;

    @Before
    public void setup() {
        FeeResponse feeResponse = FeeResponse.builder()
                .amount(TEST_FEE_AMOUNT)
                .feeCode(TEST_FEE_CODE)
                .version(TEST_FEE_VERSION)
                .description(TEST_FEE_DESCRIPTION)
                .build();
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);

        FeeValue feeValue = new FeeValue();
        feeValue.setFeeAmount(TEST_FEE_AMOUNT.toString());
        feeValue.setFeeCode(TEST_FEE_CODE);
        feeValue.setFeeVersion(TEST_FEE_VERSION.toString());
        feeValue.setFeeDescription(TEST_FEE_DESCRIPTION);

        caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);
        caseData.put(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary);
        caseData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        caseData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.EASTMIDLANDS.getSiteId());
        caseData.put(SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY, TEST_SOLICITOR_ACCOUNT_NUMBER);
        caseData.put(SOLICITOR_FIRM_JSON_KEY, TEST_SOLICITOR_FIRM_NAME);
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);

        request = new CreditAccountPaymentRequest();
        request.setService(SERVICE);
        request.setCurrency(CURRENCY);
        request.setAmount(orderSummary.getPaymentTotal());
        request.setCcdCaseNumber(TEST_CASE_ID);
        request.setSiteId(CourtEnum.EASTMIDLANDS.getSiteId());
        request.setAccountNumber(TEST_SOLICITOR_ACCOUNT_NUMBER);
        request.setOrganisationName(TEST_SOLICITOR_FIRM_NAME);
        request.setCustomerReference(TEST_SOLICITOR_REFERENCE);
        request.setDescription(TEST_FEE_DESCRIPTION);

        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setCcdCaseNumber(TEST_CASE_ID);
        paymentItem.setCalculatedAmount(orderSummary.getPaymentTotal());
        paymentItem.setCode(TEST_FEE_CODE);
        paymentItem.setReference(TEST_SOLICITOR_REFERENCE);
        paymentItem.setVersion(TEST_FEE_VERSION.toString());
        request.setFees(Collections.singletonList(paymentItem));
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, YES_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, YES_VALUE);

        caseDetails = CaseDetails.builder()
                        .caseData(caseData)
                        .caseId(TEST_CASE_ID)
                        .state(TEST_STATE)
                        .build();

        ccdCallbackRequest = CcdCallbackRequest.builder()
                        .caseDetails(caseDetails)
                        .build();

        final CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(caseData)
                .build();

        stubCreditAccountPayment(HttpStatus.OK, new CreditAccountPaymentResponse());
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
        stubFormatterServerEndpoint(caseData);

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    @Test
    public void givenInvalidCaseData_whenProcessPbaPayment_thenReturnErrors() throws Exception {
        caseData.put(STATEMENT_OF_TRUTH, NO_VALUE);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, NO_VALUE);

        caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .build();

        ccdCallbackRequest = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .errors(Collections.singletonList(
                        "Statement of truth for solicitor and petitioner needs to be accepted"
                ))
                .build();

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    private void stubCreditAccountPayment(HttpStatus status, CreditAccountPaymentResponse response) {
        paymentsServer.stubFor(WireMock.post(PAYMENTS_CREDIT_ACCOUNT_CONTEXT_PATH)
                .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
                .withHeader(SERVICE_AUTHORIZATION_HEADER, new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
                .withRequestBody(equalToJson(convertObjectToJsonString(request)))
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(response))));
    }

    private void stubServiceAuthProvider(HttpStatus status, String response) {
        serviceAuthProviderServer.stubFor(WireMock.post(SERVICE_AUTH_CONTEXT_PATH)
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withBody(response)));
    }

    private void stubFormatterServerEndpoint(Map<String , Object> data) {
        formatterServiceServer.stubFor(WireMock.post(FORMAT_REMOVE_PETITION_DOCUMENTS_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(data)))
            .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                    .withBody(convertObjectToJsonString(data))));
    }
}
