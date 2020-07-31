package uk.gov.hmcts.reform.divorce.orchestration.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "uk.gov.notify.email")
@Validated
@Getter
public class EmailTemplatesConfig {
    @NotNull
    private Map<LanguagePreference, Map<String, String>> templates = new HashMap<>();

    @NotNull
    private Map<String, Map<String, String>> templateVars = new HashMap<>();
}
