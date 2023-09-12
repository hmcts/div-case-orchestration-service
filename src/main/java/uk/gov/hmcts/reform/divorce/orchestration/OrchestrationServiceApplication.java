package uk.gov.hmcts.reform.divorce.orchestration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.authorisation.ServiceAuthAutoConfiguration;
import uk.gov.hmcts.reform.divorce.orchestration.config.CourtDetailsConfig;
import uk.gov.hmcts.reform.divorce.orchestration.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation.CourtDistributionConfig;
import uk.gov.hmcts.reform.sendletter.SendLetterAutoConfiguration;

import javax.annotation.PostConstruct;

@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.divorce",
    "uk.gov.hmcts.reform.idam.client",
    "uk.gov.hmcts.reform.sendletter"
})
@SpringBootApplication(
    scanBasePackages = {
        "uk.gov.hmcts.reform.bsp.common",
        "uk.gov.hmcts.reform.divorce",
        "uk.gov.hmcts.reform.divorce.scheduler.service",
        "uk.gov.hmcts.reform.logging.appinsights"
    },
    exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        SendLetterAutoConfiguration.class,
        ServiceAuthAutoConfiguration.class
    }
)
@EnableConfigurationProperties({CourtDetailsConfig.class, CourtDistributionConfig.class, EmailTemplatesConfig.class})
@Slf4j
public class OrchestrationServiceApplication {

    @Value("${spring.datasource.scheduler.username}")
    private String schedulerUsername;

    @Value("${spring.datasource.scheduler.password}")
    private String schedulerPassword;

    public static void main(String[] args) {
        SpringApplication.run(OrchestrationServiceApplication.class, args);
    }

    @PostConstruct
    private void init() {
        log.info("Scheduler userName----------------------------- {}",schedulerUsername);
        log.info("Scheduler password----------------------------- {}",schedulerPassword);
    }
}
