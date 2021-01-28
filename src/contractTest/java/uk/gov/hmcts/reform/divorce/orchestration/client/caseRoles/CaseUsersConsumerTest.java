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
@PactTestFor(providerName = "ccdDataApi_caseUsers", port = "8893")
@PactFolder("pacts")
@SpringBootTest({
    "ccd.data-store.api.url : localhost:8893"
})
public class CaseUsersConsumerTest {


    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final RemoveUserRolesRequest REMOVE_USER_ROLES_REQUEST = buildRemoveUserRolesRequest();

    @Autowired
    private CaseRoleClient caseRoleClient;

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

    @Pact(provider = "ccdDataApi_caseUsers", consumer = "divorce_caseOrchestratorService")
    RequestResponsePact removeCaseRoles(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("User exists")
            .uponReceiving("a request to remove given case roles from that user")
            .path("/case-users")
            .method("DELETE")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .willRespondWith()
            .status(200)
            .body(buildCaseAssignedUserRolesResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "removeCaseRoles")
    public void verifyRemoveCaseRolesPact() throws JSONException {
        Map<String, Object> response = caseRoleClient.removeCaseRoles(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, REMOVE_USER_ROLES_REQUEST);
        assertThat(response.get("status_message"), equalTo("Case-User-Role assignments removed successfully"));

    }

    private DslPart buildCaseAssignedUserRolesResponse() {
        return newJsonBody((o) -> {
                .stringType("status_message", "Case-User-Role assignments removed successfully"));
        }).build();
    }

    private RemoveUserRolesRequest buildRemoveUserRolesRequest(){
        return RemoveUserRolesRequest
            .builder()
            .caseUsers(
                Collections.singletonList(
                    CaseUser.builder()
                        .caseId(TEST_CASE_ID)
                        .userId(TEST_USER_ID)
                        .caseRole(CREATOR_CASE_ROLE)
                        .build()
                )
            ).build()
    }
}
