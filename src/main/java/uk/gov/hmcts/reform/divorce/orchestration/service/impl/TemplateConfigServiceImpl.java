package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.config.TemplateConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;

import java.util.Locale;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TEMPLATE_RELATION;


@Service
@Slf4j
public class TemplateConfigServiceImpl implements TemplateConfigService {

    @Autowired
    private TemplateConfig templateConfig;

    @Override
    public String getRelationshipTermByGender(String gender, LanguagePreference languagePreference) {
        if (gender == null) {
            return null;
        }
        return templateConfig.getTemplate().get(TEMPLATE_RELATION).get(languagePreference).get(gender.toLowerCase(Locale.ENGLISH));
    }
}
