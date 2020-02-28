package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class DraftDataUtilsTest {

    @Test
    public void getTestLanguagePreferenceWithValue_Yes() {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        Optional<LanguagePreference> languagePreference = DraftDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(Optional.of(LanguagePreference.WELSH)));
    }

    @Test
    public void getTestLanguagePreferenceWithValue_No() {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO_VALUE);
        Optional<LanguagePreference> languagePreference = DraftDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(Optional.of(LanguagePreference.ENGLISH)));
    }

    @Test
    public void getTestLanguagePreferenceWithValue_NULL() {
        HashMap<String, Object> caseData = new HashMap<>();
        Optional<LanguagePreference> languagePreference = DraftDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(Optional.of(LanguagePreference.ENGLISH)));
    }

    @Test
    public void getTestLanguagePreference_Null_DraftData() {
        HashMap<String, Object> caseData = null;
        Optional<LanguagePreference> languagePreference = DraftDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(Optional.of(LanguagePreference.ENGLISH)));
    }
}
