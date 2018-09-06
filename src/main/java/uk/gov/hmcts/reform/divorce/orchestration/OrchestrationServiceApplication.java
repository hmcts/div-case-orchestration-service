package uk.gov.hmcts.reform.divorce.orchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.authorisation.healthcheck.ServiceAuthHealthIndicator;

@SpringBootApplication(exclude = {ServiceAuthHealthIndicator.class})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.divorce"})
public class OrchestrationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrchestrationServiceApplication.class, args);
    }
}