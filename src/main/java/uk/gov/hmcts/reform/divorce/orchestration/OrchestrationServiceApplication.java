package uk.gov.hmcts.reform.divorce.orchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.authorisation.healthcheck.ServiceAuthHealthIndicator;
import uk.gov.hmcts.reform.sendletter.SendLetterAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.divorce",  "uk.gov.hmcts.reform.logging.appinsights"},
    exclude = {ServiceAuthHealthIndicator.class, SendLetterAutoConfiguration.class})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.divorce", "uk.gov.hmcts.reform.sendletter"})
public class OrchestrationServiceApplication {
    public static void main(String[] args) {
        System.setProperty("http.proxyHost", "proxyout.reform.hmcts.net");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyHost", "proxyout.reform.hmcts.net");
        System.setProperty("https.proxyPort", "8080");
        SpringApplication.run(OrchestrationServiceApplication.class, args);
    }
}