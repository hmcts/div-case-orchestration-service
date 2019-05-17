package uk.gov.hmcts.reform.divorce.orchestration.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "uk.gov.notify.email")
@Validated
@Getter
public class EmailTemplatesConfig {
    private Map<String, String> templates = new HashMap<>();
    private Map<String, String> templateVars = new HashMap<>();
}
