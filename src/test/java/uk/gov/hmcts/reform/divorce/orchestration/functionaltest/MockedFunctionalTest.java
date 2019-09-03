package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;


@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public abstract class MockedFunctionalTest {

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

    private static WireMockConfiguration buildWireMockConfig(int port) {
        return WireMockSpring
                .options()
                .port(port)
                .extensions(new ConnectionCloseExtension());
    }
}
