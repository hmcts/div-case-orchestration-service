package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.config.DocumentTemplates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentTemplateService {
    private static final String ERROR_MESSAGE = "No template found for languagePreference %s and template name %s";
    private final DocumentTemplates documentTemplates;

    public String getTemplateId(Optional<LanguagePreference> languagePreference, DocumentType documentType) {
        LanguagePreference languagePref = languagePreference.orElse(LanguagePreference.ENGLISH);
        return Optional.ofNullable(documentTemplates.getTemplates().get(languagePref).get(documentType.getTemplateName()))
                .orElseThrow(() -> new IllegalArgumentException(String.format(ERROR_MESSAGE, languagePref.getCode(),
                        documentType.getTemplateName())));
    }
}
