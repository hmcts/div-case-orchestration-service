package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.config.DocumentTemplates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentTemplateService {
    private final DocumentTemplates documentTemplates;

    public String getTemplateId(LanguagePreference languagePreference, DocumentType documentType) {
        return documentTemplates.getTemplates().get(languagePreference).get(documentType.getTemplateName());
    }
}
