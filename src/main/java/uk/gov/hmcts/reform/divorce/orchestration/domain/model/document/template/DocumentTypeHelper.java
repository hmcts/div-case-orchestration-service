package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentTypeHelper {

    public static String getLanguageAppropriateTemplate(Map<String, Object> caseData, DocumentType documentType) {
        LanguagePreference preferredLanguage = CaseDataUtils.getLanguagePreference(caseData);
        return documentType.getTemplateByLanguage(preferredLanguage);
    }

}