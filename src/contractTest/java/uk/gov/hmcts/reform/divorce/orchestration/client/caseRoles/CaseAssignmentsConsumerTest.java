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
import uk.gov.hmcts.reform.divorce.orchestration.client.AssignCaseAccessClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AssignCaseAccessRequest;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;

import static org.springframework.http.HttpMethod.POST;

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
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "aca_caseAssignments", consumer = "divorce_caseOrchestratorService")
    RequestResponsePact assignCaseAccess(PactDslWithProvider builder) throws Exception {
        return builder
            .given("Case exists")
            .uponReceiving("a request to assign case access")
            .path("/case-assignments")
            .method(POST.name())
            .headers(
                HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN
            ).body(objectMapper.writeValueAsString(ASSIGN_CASE_ACCESS_REQUEST))
            .willRespondWith()
            .status(HttpStatus.CREATED.value())
            .toPact();
    }

    private static AssignCaseAccessRequest buildAssignCaseAccessRequest() {
        return AssignCaseAccessRequest
            .builder()
            .caseId(TEST_CASE_ID)
            .assigneeId(TEST_USER_ID)
            .caseTypeId(CASE_TYPE_ID)
            .build();
    }
}
