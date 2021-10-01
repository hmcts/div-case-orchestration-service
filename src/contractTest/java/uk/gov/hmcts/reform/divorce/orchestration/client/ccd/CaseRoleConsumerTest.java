package uk.gov.hmcts.reform.divorce.orchestration.client.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.divorce.model.ccd.roles.CaseRoles;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseRoleClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseUser;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.RemoveUserRolesRequest;

import java.io.IOException;
import java.util.List;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "ccdDataStoreAPI_caseAssignedUserRoles", port = "8891")
@PactFolder("pacts")
@SpringBootTest({
    "ccd.data-store.api.url : localhost:8891"
})
public class CaseRoleConsumerTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final long CASE_ID = 1583841721773828L;
    private static final String USER_ID = "userId";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CaseRoleClient caseRoleClient;


    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "ccdDataStoreAPI_caseAssignedUserRoles", consumer = "divorce_caseOrchestratorService")
    public RequestResponsePact generatePactFragmentForDelete(PactDslWithProvider builder) throws IOException {
        // @formatter:off
        return builder
            .given("A User Role exists for a Case")
            .uponReceiving("A Request to remove a User Role")
            .method("DELETE")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/case-users")
            .body(createJsonObject(buildRemoveUserRolesRequest()))
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForDelete")
    public void verifyRemoveRoles() {
        caseRoleClient.removeCaseRoles(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN,
            buildRemoveUserRolesRequest());

    }

    private RemoveUserRolesRequest buildRemoveUserRolesRequest() {
        return RemoveUserRolesRequest.builder()
            .caseUsers(List.of(CaseUser.builder()
                .userId(USER_ID)
                .caseRole(CaseRoles.CREATOR)
                .caseId(Long.toString(CASE_ID))
                .build()))
            .build();
    }

    private String createJsonObject(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }
}
