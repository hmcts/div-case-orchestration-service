package uk.gov.hmcts.reform.divorce.orchestration.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.Map;
import javax.validation.Valid;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties()
public class TemplateConfig {
    @Valid
    private Map<String, Map<LanguagePreference,  Map<String, String>>> template;
}