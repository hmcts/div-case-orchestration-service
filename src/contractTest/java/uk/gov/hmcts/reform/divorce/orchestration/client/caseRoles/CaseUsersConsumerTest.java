package uk.gov.hmcts.reform.divorce.orchestration.client.caseRoles;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.fluent.Executor;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseRoleClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseUser;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.RemoveUserRolesRequest;

import java.util.Collections;

import static org.springframework.http.HttpMethod.DELETE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.service.CcdDataStoreService.CREATOR_CASE_ROLE;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "ccdDataApi_caseUsers", port = "8893")
@PactFolder("pacts")
@SpringBootTest( {
    "ccd.data-store.api.url : localhost:8893"
})
public class CaseUsersConsumerTest {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final RemoveUserRolesRequest REMOVE_USER_ROLES_REQUEST = buildRemoveUserRolesRequest();

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "ccdDataApi_caseUsers", consumer = "divorce_caseOrchestratorService")
    RequestResponsePact removeCaseRoles(PactDslWithProvider builder) throws Exception {
        return builder
            .given("User with [CREATOR] case role exists")
            .uponReceiving("a request to remove given case roles from that user")
            .path("/case-users")
            .method(DELETE.name())
            .headers(
                HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN
            ).body(objectMapper.writeValueAsString(REMOVE_USER_ROLES_REQUEST))
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .toPact();
    }

    private static RemoveUserRolesRequest buildRemoveUserRolesRequest() {
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
            ).build();
    }
}
