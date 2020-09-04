package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class DraftDataUtilsTest {

    @Test
    public void getTestLanguagePreferenceWithValue_Yes() {
        HashMap<String, Object> draftData = new HashMap<>();
        draftData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        LanguagePreference languagePreference = DraftDataUtils.getLanguagePreference(draftData);
        assertThat(languagePreference, is(LanguagePreference.WELSH));
    }

    @Test
    public void getTestLanguagePreferenceWithValue_No() {
        HashMap<String, Object> draftData = new HashMap<>();
        draftData.put(LANGUAGE_PREFERENCE_WELSH, NO_VALUE);
        LanguagePreference languagePreference = DraftDataUtils.getLanguagePreference(draftData);
        assertThat(languagePreference, is(LanguagePreference.ENGLISH));
    }

    @Test
    public void getTestLanguagePreferenceWithValue_NULL() {
        HashMap<String, Object> draftData = new HashMap<>();
        draftData.put(LANGUAGE_PREFERENCE_WELSH, null);
        LanguagePreference languagePreference = DraftDataUtils.getLanguagePreference(draftData);
        assertThat(languagePreference, is(LanguagePreference.ENGLISH));
    }

    @Test
    public void getTestLanguagePreferenceNotSet() {
        HashMap<String, Object> draftData = new HashMap<>();
        LanguagePreference languagePreference = DraftDataUtils.getLanguagePreference(draftData);
        assertThat(languagePreference, is(LanguagePreference.ENGLISH));
    }

    @Test
    public void getTestLanguagePreference_Null_DraftData() {
        HashMap<String, Object> draftData = null;
        LanguagePreference languagePreference = DraftDataUtils.getLanguagePreference(draftData);
        assertThat(languagePreference, is(LanguagePreference.ENGLISH));
    }
}
