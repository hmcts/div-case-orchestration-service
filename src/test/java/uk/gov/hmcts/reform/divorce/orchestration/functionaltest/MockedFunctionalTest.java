package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@ContextConfiguration(classes = OrchestrationServiceApplication.class,
        initializers = MockedFunctionalTest.RandomPortInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class MockedFunctionalTest {

    @ClassRule
    public static WireMockClassRule maintenanceServiceServer = new WireMockClassRule(wireMockConfig().dynamicPort());

    @ClassRule
    public static WireMockClassRule documentGeneratorServiceServer = new WireMockClassRule(wireMockConfig().dynamicPort());

    @ClassRule
    public static WireMockClassRule featureToggleService = new WireMockClassRule(wireMockConfig().dynamicPort());

    @ClassRule
    public static WireMockClassRule feesAndPaymentsServer = new WireMockClassRule(wireMockConfig().dynamicPort());

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(wireMockConfig().dynamicPort());

    @ClassRule
    public static WireMockClassRule idamServer = new WireMockClassRule(wireMockConfig().dynamicPort());

    @ClassRule
    public static WireMockClassRule paymentServiceServer = new WireMockClassRule(wireMockConfig().dynamicPort());

    @ClassRule
    public static WireMockClassRule sendLetterService = new WireMockClassRule(wireMockConfig().dynamicPort());

    @ClassRule
    public static WireMockClassRule serviceAuthProviderServer = new WireMockClassRule(wireMockConfig().dynamicPort());

    @ClassRule
    public static WireMockClassRule validationServiceServer = new WireMockClassRule(wireMockConfig().dynamicPort());

    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "case.maintenance.service.api.baseurl=" + "http://localhost:" + maintenanceServiceServer.port(),
                "document.generator.service.api.baseurl=" + "http://localhost:" + documentGeneratorServiceServer.port(),
                "feature-toggle.service.api.baseurl=" + "http://localhost:" + featureToggleService.port(),
                "fees-and-payments.service.api.baseurl=" + "http://localhost:" + feesAndPaymentsServer.port(),
                "case.formatter.service.api.baseurl=" + "http://localhost:" + formatterServiceServer.port(),
                "idam.api.url=" + "http://localhost:" + idamServer.port(),
                "payment.service.api.baseurl=" + "http://localhost:" + paymentServiceServer.port(),
                "send-letter.url=" + "http://localhost:" + sendLetterService.port(),
                "idam.s2s-auth.url=" + "http://localhost:" + serviceAuthProviderServer.port(),
                "case.validation.service.api.baseurl=" + "http://localhost:" + validationServiceServer.port()
            );
        }
    }
}
