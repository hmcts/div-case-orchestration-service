package uk.gov.hmcts.reform.divorce.orchestration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import javax.validation.Valid;
import java.util.Map;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties("documents")
public class DocumentTemplates {
    @Valid
    private Map<LanguagePreference, Map<String, String>> templates;
}
