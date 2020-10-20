package uk.gov.hmcts.reform.divorce.orchestration.client;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentItem;

import java.io.IOException;
import java.util.Collections;
import javax.validation.constraints.NotNull;

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

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "payment_creditAccountPayment", port = "8891")
@PactFolder("pacts")
@SpringBootTest({
    "payment.service.api.baseurl : localhost:8891"
})
public class CreditAccountPaymentsConsumerTest {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    ObjectMapper objectMapper;

    public static final String  SERVICE_AUTHORIZATION = "ServiceAuthorization";

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
            .given("A credit account payment does not exist")
            .uponReceiving("a request to create a credit account payment")
            .path("/credit-account-payments")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .body(objectMapper.writeValueAsString(getCreditAccountPaymentRequest()))
            .willRespondWith()
            .status(201)
            .body(buildCreditAccountPaymentResponseDtoPactDsl())
            .toPact();
    }

    private DslPart buildCreditAccountPaymentResponseDtoPactDsl() {
        return newJsonBody((o) -> {
            o.stringMatcher("date_created", "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                "2020-10-06T18:54:48.785+0000")
                .stringType("reference", "BJMSDFDS80808")
                .stringType("payment_group_reference", "2020-1602010488596")
                .stringType("status", "Success")
                .minArrayLike("status_histories", 0, 1,
                    status ->
                        status.stringMatcher("date_updated",
                            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                            "2020-10-06T18:54:48.785+0000")
                            .stringMatcher("date_created",
                                "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                                "2020-10-06T18:54:48.785+0000")
                            .stringMatcher("status",
                                "created|started|submitted|success|failed|cancelled|error|pending|decline", "success"));
        }).build();
    }

    @Test
    @PactTestFor(pactMethod = "postCreditAccountPayment")
    public void verifyPostCreditAccountPayment() throws IOException, JSONException {
        CreditAccountPaymentRequest expectedRequest = getCreditAccountPaymentRequest();
        ResponseEntity<CreditAccountPaymentResponse> response =
            paymentClient.creditAccountPayment(SOME_AUTHORIZATION_TOKEN,
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
