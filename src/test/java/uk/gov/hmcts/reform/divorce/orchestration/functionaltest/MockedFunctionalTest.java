package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;


@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public abstract class MockedFunctionalTest {

    private static final String APPLICATION_VND_UK_GOV_HMCTS_LETTER_SERVICE_IN_LETTER = "application/vnd.uk.gov.hmcts.letter-service.in.letter";

    @ClassRule
    public static WireMockClassRule maintenanceServiceServer = new WireMockClassRule(buildWireMockConfig(4010));

    @ClassRule
    public static WireMockClassRule documentGeneratorServiceServer = new WireMockClassRule(buildWireMockConfig(4007));

    @ClassRule
    public static WireMockClassRule featureToggleService = new WireMockClassRule(buildWireMockConfig(4028));

    @ClassRule
    public static WireMockClassRule feesAndPaymentsServer = new WireMockClassRule(buildWireMockConfig(4009));

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(buildWireMockConfig(4011));

    @ClassRule
    public static WireMockClassRule idamServer = new WireMockClassRule(buildWireMockConfig(4503));

    @ClassRule
    public static WireMockClassRule paymentServiceServer = new WireMockClassRule(buildWireMockConfig(9190));

    @ClassRule
    public static WireMockClassRule sendLetterService = new WireMockClassRule(buildWireMockConfig(4021));

    @ClassRule
    public static WireMockClassRule serviceAuthProviderServer = new WireMockClassRule(buildWireMockConfig(4504));

    @ClassRule
    public static WireMockClassRule validationServiceServer = new WireMockClassRule(buildWireMockConfig(4008));

    @ClassRule
    public static WireMockClassRule documentStore = new WireMockClassRule(4020);

    private static WireMockConfiguration buildWireMockConfig(int port) {
        return WireMockSpring
            .options()
            .port(port)
            .extensions(new ConnectionCloseExtension());
    }

    protected void stubSendLetterService(HttpStatus status) {
        sendLetterService.stubFor(WireMock.post("/letters")
            .withHeader("ServiceAuthorization", new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
            .withHeader("Content-Type", new EqualToPattern(APPLICATION_VND_UK_GOV_HMCTS_LETTER_SERVICE_IN_LETTER
                + ".v2+json"))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withBody(convertObjectToJsonString(new SendLetterResponse(UUID.randomUUID())))));
    }

}