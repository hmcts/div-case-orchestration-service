package uk.gov.hmcts.reform.divorce.orchestration.util;


import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;


public class DraftDataUtils {

    private DraftDataUtils() {

    }

    public static LanguagePreference getLanguagePreference(Map<String, Object> draft) {
        return Optional.ofNullable(draft)
                .map(data -> data.get(LANGUAGE_PREFERENCE_WELSH))
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .filter(YES_VALUE::equalsIgnoreCase)
                .map(languagePreferenceWelsh -> LanguagePreference.WELSH)
                .orElse(LanguagePreference.ENGLISH);
    }
}
