package uk.gov.hmcts.reform.divorce.orchestration.client;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CURRENCY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;

import org.apache.http.client.fluent.Executor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentItem;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "payment_creditAccountPayment", port = "8891")
@PactFolder("pacts")
@SpringBootTest( {
    "payment.service.api.baseurl : localhost:8891"
})
public class CreditAccountPaymentsConsumerTest {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    private static final String ACCESS_TOKEN = "someAccessToken";
    private static final String TOKEN = "someToken";

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    ObjectMapper objectMapper;

    private Long USER_ID = 123456L;
    private String PAYMENT_REFERENCE = "654321ABC";
    private String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private String EXPERIMENTAL = "experimental=true";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "payment_creditAccountPayment", consumer = "divorce_caseOrchestratorService")
    RequestResponsePact postCreditAccountPayment(PactDslWithProvider builder) throws JsonProcessingException {
        // @formatter:off
        return builder
            .given("A payment reference exists")
            .uponReceiving("a request for information for that payment reference")
            .path("/credit-account-payments")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN)
            .body(objectMapper.writeValueAsString(getCreditAccountPaymentRequest()))
            .willRespondWith()
            .status(201)
            .body(buildCreditAccountPaymentResponseDtoPactDsl())
            .toPact();
    }

    private DslPart buildCreditAccountPaymentResponseDtoPactDsl() {

        return newJsonBody((o) -> {
            o.numberType("amount", 500)
                .stringType("account_number", "12398639674")
                .stringType("ccd_case_number", "2138832982922")
                .minArrayLike("fees", 0, 1,
                    fee -> fee.object("value", (value) ->
                        value.stringType("FeeDescription", "This is the fee description")
                            .stringType("FeeVersion", "4")
                            .stringType("FeeCode", "FEE003")
                            .stringType("FeeAmount", "550.00")
                    ))
                .stringMatcher("date_updated", "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{3})Z$","2020-01-01T00:00:000Z")
                    .stringType("method", "online")
                        .minArrayLike("status_histories", 0, 1,
                            status ->
                                status.stringMatcher("date_updated", "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{3})Z$","2020-01-01T00:00:000Z")
                                    .stringMatcher("date_created", "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{3})Z$","2020-01-01T00:00:000Z")
                                    .stringType("external_status", "Success")
                                    .stringMatcher("status","Initiated|Success|Failed|Pending|Declined", "Success")
                            )
                    .stringMatcher("date_created", "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{3})Z$","2020-01-01T00:00:000Z")
                    .stringType("service_name", "ccd_gw")
                        .stringType("channel", "online")
                    .stringType("description", "This is a description")
                        .stringType("organisation_name", "Organisation name")
                        .stringType("payment_reference", "paymentReference")
                        .stringType("external_provider", "external provider")
                        .stringType("reference", "BJMSDFDS80808")
                        .stringType("case_reference", "2131323232312323")
                        .stringType("customer_reference", "BJHDA123213SREF")
                        .stringType("external_reference", "BJHDA123213SREF")
                        .stringType("site_id", "siteId")
                        .stringType("payment_group_reference", "BJMSDFDS80808FREF")
                        .stringType("currency", "GBP")
                        .stringType("id", "213456")
                        .stringType("status", "Success");

        }).build();
    }


    @Test
    @PactTestFor(pactMethod = "postCreditAccountPayment")
    public void verifyPostCreditAccountPayment() throws IOException, JSONException {


        CreditAccountPaymentRequest expectedRequest = getCreditAccountPaymentRequest();


        ResponseEntity<CreditAccountPaymentResponse> response = paymentClient.creditAccountPayment(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, expectedRequest);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));

    }

    @NotNull
    private CreditAccountPaymentRequest getCreditAccountPaymentRequest() {
        CreditAccountPaymentRequest expectedRequest = new CreditAccountPaymentRequest();
        expectedRequest.setService(SERVICE);
        expectedRequest.setCurrency(CURRENCY);
        expectedRequest.setAmount("500.00");
        expectedRequest.setCcdCaseNumber(TEST_CASE_ID);
        expectedRequest.setSiteId(CourtEnum.EASTMIDLANDS.getSiteId());
        expectedRequest.setAccountNumber(TEST_SOLICITOR_ACCOUNT_NUMBER);
        expectedRequest.setOrganisationName(TEST_SOLICITOR_FIRM_NAME);
        expectedRequest.setCustomerReference(TEST_SOLICITOR_REFERENCE);
        expectedRequest.setDescription(TEST_FEE_DESCRIPTION);

        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setCcdCaseNumber(TEST_CASE_ID);
        paymentItem.setCalculatedAmount("550.00");
        paymentItem.setCode(TEST_FEE_CODE);
        paymentItem.setReference(TEST_SOLICITOR_REFERENCE);
        paymentItem.setVersion(TEST_FEE_VERSION.toString());
        expectedRequest.setFees(Collections.singletonList(paymentItem));
        return expectedRequest;
    }
}
