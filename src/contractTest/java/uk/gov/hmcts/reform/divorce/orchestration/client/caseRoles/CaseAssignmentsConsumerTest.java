package uk.gov.hmcts.reform.divorce.orchestration.client.caseRoles;

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
import uk.gov.hmcts.reform.divorce.orchestration.client.PaymentClient;

import java.io.IOException;
import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "aca_caseAssignments", port = "8894")
@PactFolder("pacts")
@SpringBootTest({
    "aca.api.url : localhost:8893"
})
public class CaseAssignmentsConsumerTest {


    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final AssignCaseAccessRequest ASSIGN_CASE_ACCESS_REQUEST = buildAssignCaseAccessRequest();

    @Autowired
    private AssignCaseAccessClient assignCaseAccessClient;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "aca_caseAssignments", consumer = "divorce_caseOrchestratorService")
    RequestResponsePact assignCaseAccess(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Case exists")
            .uponReceiving("a request to assign case access")
            .path("/case-assignments")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .willRespondWith()
            .status(201)
            .body(buildCaseAssignmentResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "assignCaseAccess")
    public void verifyAssignCaseAccessPact() throws JSONException {
        Map<String, Object> response = assignCaseAccessClient.assignCaseAccess(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, ASSIGN_CASE_ACCESS_REQUEST);
        assertThat(response.get("status_message"), equalTo("Role from the organisation policy successfully assigned to the assignee."));

    }

    private DslPart buildCaseAssignmentResponse() {
        return newJsonBody((o) -> {
                .stringType("status_message", "Role from the organisation policy successfully assigned to the assignee."));
        }).build();
    }

    private AssignCaseAccessRequest buildAssignCaseAccessRequest() {
        return AssignCaseAccessRequest
            .builder()
            .caseId(TEST_CASE_ID)
            .assigneeId(TEST_USER_ID)
            .caseTypeId(CASE_TYPE_ID)
            .build()
    }
}
