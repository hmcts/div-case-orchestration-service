package uk.gov.hmcts.reform.divorce.orchestration.client.payments;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
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
import uk.gov.hmcts.reform.divorce.orchestration.client.PaymentClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CreditAccountPaymentRequestBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

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
    private CreditAccountPaymentRequestBuilder creditAccountPaymentRequestBuilder;

    @Autowired
    ObjectMapper objectMapper;

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    protected TaskContext context;
    protected Map<String, Object> caseData;
    protected CreditAccountPaymentRequest expectedRequest;
    protected OrderSummary orderSummary;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
        context = contextWithToken();

        FeeResponse feeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();
        orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);

        caseData = new HashMap<>();
        caseData.put(SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY, TEST_SOLICITOR_ACCOUNT_NUMBER);
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);
        caseData.put(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary);
        caseData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        caseData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.EASTMIDLANDS.getSiteId());
        caseData.put(PETITIONER_SOLICITOR_FIRM, TEST_SOLICITOR_FIRM_NAME);
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(CASE_TYPE_ID, "DIVORCE");
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "payment_creditAccountPayment", consumer = "divorce_caseOrchestratorService")
    RequestResponsePact postCreditAccountPayment(PactDslWithProvider builder) throws JsonProcessingException {
        return buildRequestResponsePact(builder, "An active account has sufficient funds for a payment", "50000", 201, "Success", "success", null,
            "Payment request failed . PBA account CAERPHILLY COUNTY BOROUGH COUNCIL have insufficient funds available");
    }


    @Pact(provider = "payment_creditAccountPayment", consumer = "divorce_caseOrchestratorService")
    RequestResponsePact postCreditAccountPaymentForbidden(PactDslWithProvider builder) throws JsonProcessingException {
        return buildRequestResponsePact(builder, "An active account has insufficient funds for a payment", "150000", 403, "Failed", "failed",
            "CA-E0001", "Payment request failed . PBA account CAERPHILLY COUNTY BOROUGH COUNCIL have insufficient funds available");
    }

    @Pact(provider = "payment_creditAccountPayment", consumer = "divorce_caseOrchestratorService")
    RequestResponsePact postCreditAccountPaymentOnHold(PactDslWithProvider builder) throws JsonProcessingException {
        return buildRequestResponsePact(builder, "An on hold account requests a payment", "150000", 403, "Failed", "failed",
            "CA-E0003", "Your account is on hold");
    }

    @Pact(provider = "payment_creditAccountPayment", consumer = "divorce_caseOrchestratorService")
    RequestResponsePact postCreditAccountPaymentDeleted(PactDslWithProvider builder) throws JsonProcessingException {
        return buildRequestResponsePact(builder, "A deleted account requests a payment", "150000", 403, "Failed", "failed",
            "CA-E0004", "Your account is deleted");
    }

    private RequestResponsePact buildRequestResponsePact(PactDslWithProvider builder, String stateName, String amount,
                                                         int statusCode, String status, String paymentStatus,
                                                         String errorCode, String errorMessage)
        throws JsonProcessingException {
        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("accountNumber", "test.solicitor.account");
        paymentMap.put("availableBalance", "1000.00");
        paymentMap.put("accountName", "CAERPHILLY COUNTY BOROUGH COUNCIL");
        // @formatter:off
        return builder
            .given(stateName, paymentMap)
            .uponReceiving("a request to  credit that account with a payment")
            .path("/credit-account-payments")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .body(objectMapper.writeValueAsString(getCreditAccountPaymentRequest(amount)))
            .willRespondWith()
            .status(statusCode)
            .body(buildCreditAccountPaymentResponseDtoPactDsl(status, paymentStatus, errorCode,
                errorMessage))
            .toPact();
    }

    private DslPart buildCreditAccountPaymentResponseDtoPactDsl(String status, String paymentStatus, String errorCode, String errorMessage) {
        return newJsonBody((o) -> {
            o.stringMatcher("date_created", "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                "2020-10-06T18:54:48.785+0000")
                .stringType("reference", "BJMSDFDS80808")
                .stringType("payment_group_reference", "2020-1602010488596")
                .stringType("status", status)
                .minArrayLike("status_histories", 1, 1,
                    (sh) -> {
                        sh.stringMatcher("date_updated",
                            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                            "2020-10-06T18:54:48.785+0000")
                            .stringMatcher("date_created",
                                "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                                "2020-10-06T18:54:48.785+0000")
                            .stringValue("status", paymentStatus);
                        if (errorCode != null) {
                            sh.stringValue("error_code", errorCode);
                            sh.stringType("error_message",
                                errorMessage);
                        }
                    });
        }).build();
    }

    @Test
    @PactTestFor(pactMethod = "postCreditAccountPayment")
    public void verifyPostCreditAccountPayment() throws IOException, JSONException {
        CreditAccountPaymentRequest expectedRequest = getCreditAccountPaymentRequest("50000");
        ResponseEntity<CreditAccountPaymentResponse> response =
            paymentClient.creditAccountPayment(SOME_AUTHORIZATION_TOKEN,
                SOME_SERVICE_AUTHORIZATION_TOKEN, expectedRequest);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
    }


    @Test
    @PactTestFor(pactMethod = "postCreditAccountPaymentForbidden")
    public void verifyPostCreditAccountPaymentForbidden() throws IOException, JSONException {
        verifyForbiddenRequest();
    }

    @Test
    @PactTestFor(pactMethod = "postCreditAccountPaymentOnHold")
    public void verifyPostCreditAccountPaymentOnHold() throws IOException, JSONException {
        verifyForbiddenRequest();
    }

    @Test
    @PactTestFor(pactMethod = "postCreditAccountPaymentDeleted")
    public void verifyPostCreditAccountPaymentDeleted() throws IOException, JSONException {
        verifyForbiddenRequest();
    }

    private void verifyForbiddenRequest() {
        CreditAccountPaymentRequest expectedRequest = getCreditAccountPaymentRequest("150000");
        Exception exception = assertThrows(FeignException.FeignClientException.class, () -> {
            ResponseEntity<CreditAccountPaymentResponse> response =
                paymentClient.creditAccountPayment(SOME_AUTHORIZATION_TOKEN,
                    SOME_SERVICE_AUTHORIZATION_TOKEN, expectedRequest);
        });
    }

    @NotNull
    private CreditAccountPaymentRequest getCreditAccountPaymentRequest(String amount) {
        CreditAccountPaymentRequest expectedRequest = creditAccountPaymentRequestBuilder.buildCreditAccountPaymentRequest(context, caseData);
        expectedRequest.setAmount(amount);
        return expectedRequest;
    }
}
