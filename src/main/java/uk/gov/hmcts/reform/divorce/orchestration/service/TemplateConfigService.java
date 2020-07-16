package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

public interface TemplateConfigService {
    String getRelationshipTermByGender(String gender, LanguagePreference languagePreference);
}