package uk.gov.hmcts.reform.divorce.orchestration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class InfrastructureConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
