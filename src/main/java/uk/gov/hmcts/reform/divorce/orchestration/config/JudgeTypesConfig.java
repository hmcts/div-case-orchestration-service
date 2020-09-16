package uk.gov.hmcts.reform.divorce.orchestration.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "judge")
@Validated
@Getter
public class JudgeTypesConfig {
    @NotNull
    private Map<String, String> types = new HashMap<>();
}
