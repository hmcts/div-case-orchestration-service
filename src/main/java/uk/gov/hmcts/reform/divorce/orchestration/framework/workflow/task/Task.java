package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Optional;

@FunctionalInterface
public interface Task<T> {
    T execute(TaskContext context, T payload) throws TaskException;

    default String getTemplateId(DocumentTemplateService documentTemplateService,
                                              DocumentType documentType, Object languagePreferenceFlag) {
        Optional<LanguagePreference> languagePreference = CaseDataUtils.getLanguagePreference(languagePreferenceFlag);
        return documentTemplateService.getTemplateId(languagePreference, documentType);
    }
}
    