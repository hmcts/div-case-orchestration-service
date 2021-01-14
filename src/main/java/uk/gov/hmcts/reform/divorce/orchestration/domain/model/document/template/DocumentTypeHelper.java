package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class DocumentTypeHelper {

    public static String getLanguageAppropriateTemplate(Map<String, Object> caseData, DocumentType documentType) {
        LanguagePreference preferredLanguage = CaseDataUtils.getLanguagePreference(caseData);
        String template = documentType.getTemplateByLanguage(preferredLanguage);
        log.debug("For language {}, using template {}", preferredLanguage, template);
        return template;
    }

}