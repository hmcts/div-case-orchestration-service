package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;
import java.util.Optional;

public interface Template {
    default String getTemplateId(DocumentTemplateService documentTemplateService,
                                 DocumentType documentType, Map<String, Object> caseData) {
        Optional<LanguagePreference> languagePreference = CaseDataUtils.getLanguagePreference(caseData);
        return documentTemplateService.getTemplateId(languagePreference, documentType);
    }
}
