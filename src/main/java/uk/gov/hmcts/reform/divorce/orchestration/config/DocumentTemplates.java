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
@ConfigurationProperties("documents")
public class DocumentTemplates {
    @Valid
    private Map<LanguagePreference, Map<String, String>> templates;
}
