package uk.gov.hmcts.reform.divorce.orchestration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "idam")
public class IdamServiceConfiguration {
    private String url;
    private String api;
}
