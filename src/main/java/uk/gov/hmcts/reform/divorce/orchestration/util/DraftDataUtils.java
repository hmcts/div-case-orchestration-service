package uk.gov.hmcts.reform.divorce.orchestration.util;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class DraftDataUtils {

    public static Optional<LanguagePreference> getLanguagePreference(Map<String, Object> draft) {
        return Optional.of(Optional.ofNullable(draft)
                .map(data -> data.get(LANGUAGE_PREFERENCE_WELSH))
                .filter(language -> language != null)
                .map(String.class::cast)
                .filter(language -> YES_VALUE.equalsIgnoreCase(language))
                .map(languagePreferenceWelsh -> LanguagePreference.WELSH)
                .orElse(LanguagePreference.ENGLISH));
    }
}
