package uk.gov.hmcts.reform.divorce.orchestration.client.referencedata;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.divorce.orchestration.client.OrganisationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.prd.OrganisationsResponse;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "referenceData_organisationalExternalUsers", port = "8892")
@PactFolder("pacts")
@SpringBootTest({
    "prd.api.url : localhost:8892"
})
public class RefDataExternalOrganisationsConsumerTest {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Autowired
    private OrganisationClient organisationClient;


    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }


    @Pact(provider = "referenceData_organisationalExternalUsers", consumer = "divorce_caseOrchestratorService")
    public RequestResponsePact generatePactFragmentForGetUserOrganisation(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Organisation with Id exists")
            .uponReceiving("A Request to get organisation for user")
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .path("/refdata/external/v1/organisations")
            .willRespondWith()
            .body(buildOrganisationResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetUserOrganisation")
    public void verifyUserOrganisation() {
        OrganisationsResponse userOrganisation =
            organisationClient.getMyOrganisation(SOME_AUTHORIZATION_TOKEN, SOME_SERVICE_AUTHORIZATION_TOKEN);
        assertThat(userOrganisation, is(notNullValue()));
        assertThat(userOrganisation.getOrganisationIdentifier(), is("someOrganisationIdentifier"));
    }

    private DslPart buildOrganisationResponseDsl() {
        return newJsonBody(o -> {
            o.stringType("name", "theKCompany")
                .stringType("organisationIdentifier", "BJMSDFDS80808")
                .stringType("companyNumber", "companyNumber")
                .stringType("organisationIdentifier", "someOrganisationIdentifier")
                .stringType("sraId", "sraId")
                .booleanType("sraRegulated", Boolean.TRUE)
                .stringType("status", "ACTIVE")
                .minArrayLike("contactInformation", 1, 1,
                    sh -> {
                        sh.stringType("addressLine1", "addressLine1")
                            .stringType("addressLine2", "addressLine2")
                            .stringType("country", "UK")
                            .stringType("postCode", "SM12SX");

                    });
        }).build();
    }

}
