package uk.gov.hmcts.reform.divorce.orchestration.client;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "payment_cardPayment", port = "8891")
@PactFolder("pacts")
@SpringBootTest( {
    "payment.service.api.baseurl : localhost:8891"
})
public class CardPaymentsConsumerTest {


    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    private static final String ACCESS_TOKEN = "someAccessToken";
    public static final String REGEX_DATE = "^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$";
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

    @Pact(provider = "payment_cardPayment", consumer = "divorce_caseOrchestratorService")
    RequestResponsePact getPaymentsByReference(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("A payment reference exists")
            .uponReceiving("a request for information for that payment reference")
            .path("/card-payments/" + PAYMENT_REFERENCE)
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN)
            //.matchHeader("experimental", "true")
            .willRespondWith()
            //.matchHeader(HttpHeaders.CONTENT_TYPE, "\\w+\\/[-+.\\w]+;charset=(utf|UTF)-8")
            .status(200)
            .body(buildPaymentDtoPactDsl())
            .toPact();
    }

    private DslPart buildPaymentDtoPactDsl() {
        return newJsonBody((o) -> {
            o.numberType("amount", 500)
                .stringType("description", "Filing an application for a divorce, nullity or civil partnership dissolution – fees order 1.2.")
                .stringType("reference", "RC-1547-0733-1813-9545")
                .stringValue("currency", "GBP")
                .stringType("ccd_case_number", "1547073120300616")
                .stringType("channel", "online")
                .stringType("method", "card")
                .stringType("external_provider", "gov pay")
                .stringType("external_reference", "06kd1v30vm45hqvggphdjqbeqa")
                .stringType("site_id", "AA04")
                .stringValue("service_name", "Divorce")
                .stringType("payment_group_reference", "2019-15470733181")
                .minArrayLike("fees", 0, 1,
                    fee -> fee.stringType("code", "FEE0002")
                        .stringType("version", "4")
                        .numberType("volume", 1)
                        .decimalType("calculated_amount", 550.0)
                );

        }).build();
    }


    @Test
    @PactTestFor(pactMethod = "getPaymentsByReference")
    public void verifyGetPaymentsByReferencePact() throws IOException, JSONException {

        Map<String, Object> response = paymentClient.checkPayment(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, PAYMENT_REFERENCE);
        assertThat(response.get("ccd_case_number"), equalTo("1547073120300616"));

    }
}
