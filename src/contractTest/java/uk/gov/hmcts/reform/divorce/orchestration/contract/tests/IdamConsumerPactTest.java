package uk.gov.hmcts.reform.divorce.orchestration.contract.tests;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.TokenExchangeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "idamClient", port = "8888")
@SpringBootTest({
      "idam.api.url : localhost:8888"
})
public class IdamConsumerPactTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("strategicIdamClient")
    private IdamClient idamConsumer;

    @Pact(state = "provider returns pin user details", provider = "idamClient", consumer = "idamConsumer")
    public RequestResponsePact createPinUserPact(PactDslWithProvider builder) {
        return builder
                .given("provider returns pin user details")
                .uponReceiving("a request to POST a pin request")
                    .path("/pin")
                    .method("POST")
                    .headers("Authorization", "someToken")
                .willRespondWith()
                    .status(200)
                    .matchHeader("Content-Type", "application/json")
                    .body(new PactDslJsonBody()
                        .stringType("pin", "pinValue")
                        .stringType("expiryDate", "expiryDateValue")
                        .stringType("userId", "userIdValue")
                    )
                .toPact();
    }

    @Pact(state = "provider returns user details", provider = "idamClient", consumer = "idamConsumer")
    public RequestResponsePact getUserDetails(PactDslWithProvider builder) {
        return builder
                .given("provider returns user details")
                .uponReceiving("a request to GET user details")
                    .path("/details")
                    .method("GET")
                    .headers("Authorization", "someToken")
                .willRespondWith()
                    .status(200)
                    .matchHeader("Content-Type", "application/json")
                    .body(new PactDslJsonBody()
                        .stringType("id", "idValue")
                    )
                .toPact();
    }

    @Pact(state = "provider returns authentication code", provider = "idamClient", consumer = "idamConsumer")
    public RequestResponsePact authenticateUser(PactDslWithProvider builder) {
        return builder
                .given("provider returns authentication code")
                .uponReceiving("a request to POST for authentication")
                    .path("/oauth2/authorize")
                    .method("POST")
                    .headers("Authorization", "someToken")
                    .query("response_type=code&client_id=divorce&redirect_uri=localhost:8889")
                .willRespondWith()
                    .status(200)
                    .matchHeader("Content-Type", "application/json")
                    .body(new PactDslJsonBody()
                            .stringType("code", "codeValue")
                    )
                .toPact();
    }

    @Pact(state = "provider returns authentication token", provider = "idamClient", consumer = "idamConsumer")
    public RequestResponsePact exchangeAuthenticationCode(PactDslWithProvider builder) {
        return builder
                .given("provider returns authentication token")
                .uponReceiving("a request to POST for an authentication jwt token")
                    .path("/oauth2/token")
                    .method("POST")
                    .query("code=codeValue&grant_type=grantType&redirect_uri=localhost:8889&client_id=divorce&client_secret=secret")
                .willRespondWith()
                    .status(200)
                    .matchHeader("Content-Type", "application/json")
                    .body(new PactDslJsonBody()
                            .stringType("access_token", "tokenValue")
                    )
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPinUserPact")
    public void verifyCreatePinUserPact() {
        PinRequest pinRequest = PinRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .build();

        Pin pin = idamConsumer.createPin(pinRequest, "someToken");
        assertEquals("pinValue", pin.getPin());
        assertEquals("expiryDateValue", pin.getExpiryDate());
        assertEquals("userIdValue", pin.getUserId());
    }

    @Test
    @PactTestFor(pactMethod = "getUserDetails")
    public void verifyGetUserDetails() {
        UserDetails userDetails = idamConsumer.retrieveUserDetails("someToken");
        assertEquals("idValue", userDetails.getId());
    }

    @Test
    @PactTestFor(pactMethod = "authenticateUser")
    public void verifyAuthenticateUser() {
        AuthenticateUserResponse authenticateUserResponse =
                idamConsumer.authenticateUser("someToken", "code", "divorce", "localhost:8889");
        assertEquals("codeValue", authenticateUserResponse.getCode());
    }

    @Test
    @PactTestFor(pactMethod = "exchangeAuthenticationCode")
    public void verifyExchangeAuthenticationCode() {
        TokenExchangeResponse tokenExchangeResponse =
                idamConsumer.exchangeCode("codeValue", "grantType", "localhost:8889", "divorce", "secret");
        assertEquals("tokenValue", tokenExchangeResponse.getAccessToken());
    }
}