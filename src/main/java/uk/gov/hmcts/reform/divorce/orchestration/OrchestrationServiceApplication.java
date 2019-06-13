package uk.gov.hmcts.reform.divorce.orchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.authorisation.ServiceAuthAutoConfiguration;
import uk.gov.hmcts.reform.divorce.orchestration.config.CourtDetailsConfig;
import uk.gov.hmcts.reform.divorce.orchestration.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation.CourtDistributionConfig;
import uk.gov.hmcts.reform.sendletter.SendLetterAutoConfiguration;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.divorce", "uk.gov.hmcts.reform.sendletter"})
@SpringBootApplication(
    scanBasePackages = {"uk.gov.hmcts.reform.divorce.scheduler.service","uk.gov.hmcts.reform.divorce", "uk.gov.hmcts.reform.logging.appinsights"},
    exclude = {ServiceAuthAutoConfiguration.class, SendLetterAutoConfiguration.class,DataSourceAutoConfiguration.class
    })
@EnableConfigurationProperties(
    {CourtDistributionConfig.class, EmailTemplatesConfig.class, CourtDetailsConfig.class})
public class OrchestrationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrchestrationServiceApplication.class, args);
    }
}