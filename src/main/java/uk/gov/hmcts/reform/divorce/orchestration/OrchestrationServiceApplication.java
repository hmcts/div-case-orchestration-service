package uk.gov.hmcts.reform.divorce.orchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.authorisation.ServiceAuthAutoConfiguration;
import uk.gov.hmcts.reform.sendletter.SendLetterAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.divorce",  "uk.gov.hmcts.reform.logging.appinsights"},
    exclude = {ServiceAuthAutoConfiguration.class, SendLetterAutoConfiguration.class})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.divorce", "uk.gov.hmcts.reform.sendletter"})
public class OrchestrationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrchestrationServiceApplication.class, args);
    }
}