package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.config.DocumentTemplates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentTemplateService {

    private static final String ERROR_MESSAGE = "No template found for languagePreference %s and template name %s";
    private final DocumentTemplates documentTemplates;

    public String getTemplateId(Map<String, Object> caseData, DocumentType documentType) {//TODO - reconsider name and parameters
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(caseData);

        return Optional.ofNullable(documentTemplates.getTemplates().get(languagePreference).get(documentType.getTemplateName()))
            .orElseThrow(
                () -> new IllegalArgumentException(String.format(ERROR_MESSAGE, languagePreference.getCode(), documentType.getTemplateName()))
            );
    }

}