package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class CaseDataUtilsTest {
    private static final String MALE_GENDER = "male";
    private static final String FEMALE_GENDER = "female";
    private static final String WELSH_MALE_GENDER_IN_RELATION = "gŵr";
    private static final String WELSH_FEMALE_GENDER_IN_RELATION = "gwraig";

    private static final String FIELD_NAME = "TestField";
    private static final String LINK_ID = "LinkId";

    @Test
    public void givenDataWithCaseLink_thenReturnLinkValue() {
        Map<String, Object> caseLinkData = ImmutableMap.of(FIELD_NAME, ImmutableMap.of(CASE_REFERENCE_FIELD, LINK_ID));
        assertThat(CaseDataUtils.getCaseLinkValue(caseLinkData, FIELD_NAME), is(LINK_ID)) ;
    }

    @Test
    public void givenFieldNoExist_whenGetCaseLinkValue_thenReturnNull() {
        Map<String, Object> caseLinkData = DUMMY_CASE_DATA;
        assertThat(CaseDataUtils.getCaseLinkValue(caseLinkData, FIELD_NAME), nullValue()) ;
    }

    @Test
    public void whenGetFieldAsStringObjectMap_thenReturnExpectedValue() {
        Map<String, Object> input = ImmutableMap.of(FIELD_NAME, DUMMY_CASE_DATA);
        assertThat(CaseDataUtils.getFieldAsStringObjectMap(input, FIELD_NAME), is(DUMMY_CASE_DATA));
    }

    @Test
    public void givenNonExistValue_whenGetFieldAsStringObjectMap_thenReturnNull() {
        Map<String, Object> input =  DUMMY_CASE_DATA;
        assertThat(CaseDataUtils.getFieldAsStringObjectMap(input, FIELD_NAME), is(nullValue()));
    }

    @Test(expected = ClassCastException.class)
    public void givenNonStringObject_whenGetFieldAsStringObjectMap_thenReturnException() {
        Map<String, Object> input =  ImmutableMap.of(FIELD_NAME, LINK_ID);
        CaseDataUtils.getFieldAsStringObjectMap(input, FIELD_NAME);
    }

    @Test
    public void whenCreateCaseLinkField_ReturnLinkField() {
        Map<String, Object> expectedLink = ImmutableMap.of(CASE_REFERENCE_FIELD, LINK_ID);
        Map<String, Object> caseLink = CaseDataUtils.createCaseLinkField(FIELD_NAME, LINK_ID);
        assertThat(caseLink.get(FIELD_NAME), is(expectedLink));
    }

    @Test
    public void givenCcdCollection_whenGetElementFromCollection_theReturnElement() {
        Map<String, Object> collectionEntry = ImmutableMap.of(VALUE_KEY, DUMMY_CASE_DATA);
        assertThat(CaseDataUtils.getElementFromCollection(collectionEntry), is(DUMMY_CASE_DATA));
    }

    @Test
    public void givenNonCcdCollectionData_whenGetElementFromCollection_theReturnNull() {
        assertThat(CaseDataUtils.getElementFromCollection(DUMMY_CASE_DATA), is(nullValue()));
    }

    @Test
    public void givenClaimCostAndDnContinueClaimCost_whenCheckPetitionerClaimCost_thenReturnTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "Yes");
        caseData.put(DN_COSTS_OPTIONS_CCD_FIELD, "Continue");
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes");
        assertThat(CaseDataUtils.isPetitionerClaimingCosts(caseData), is(true));
    }

    @Test
    public void givenNoClaimCost_whenCheckPetitionerClaimCost_thenReturnFalse() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "No");
        assertThat(CaseDataUtils.isPetitionerClaimingCosts(caseData), is(false));
    }

    @Test
    public void givenClaimCostAndDnNoContinueClaimCost_whenCheckPetitionerClaimCost_thenReturnFalse() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "No");
        caseData.put(DN_COSTS_OPTIONS_CCD_FIELD, "No");
        assertThat(CaseDataUtils.isPetitionerClaimingCosts(caseData), is(false));
    }

    @Test
    public void getTestLanguagePreferenceWithValue_Yes() {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        Optional<LanguagePreference> languagePreference = CaseDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(Optional.of(LanguagePreference.WELSH)));
    }

    @Test
    public void getTestLanguagePreferenceWithValue_No() {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO_VALUE);
        Optional<LanguagePreference> languagePreference = CaseDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(Optional.of(LanguagePreference.ENGLISH)));
    }

    @Test
    public void getTestLanguagePreferenceWithValue_NULL() {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(LANGUAGE_PREFERENCE_WELSH, null);
        Optional<LanguagePreference> languagePreference = DraftDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(Optional.of(LanguagePreference.ENGLISH)));
    }

    @Test
    public void getTestLanguagePreferenceNotSet() {
        HashMap<String, Object> caseData = new HashMap<>();
        Optional<LanguagePreference> languagePreference = DraftDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(Optional.of(LanguagePreference.ENGLISH)));
    }

    @Test
    public void getTestLanguagePreference_Null_CaseData() {
        HashMap<String, Object> caseData = null;
        Optional<LanguagePreference> languagePreference = CaseDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(Optional.of(LanguagePreference.ENGLISH)));
    }

    @Test
    public void getTestWelshMaleRelationshipTermByGender() {
        String welshMaleGender = CaseDataUtils.getWelshRelationshipTermByGender(MALE_GENDER);
        assertThat(welshMaleGender, is(WELSH_MALE_GENDER_IN_RELATION));
    }

    @Test
    public void getTestWelshFemaleRelationshipTermByGender() {
        String welshMaleGender = CaseDataUtils.getWelshRelationshipTermByGender(FEMALE_GENDER);
        assertThat(welshMaleGender, is(WELSH_FEMALE_GENDER_IN_RELATION));
    }

    @Test
    public void getTestWelshNullRelationshipTermByGender() {
        String welshMaleGender = CaseDataUtils.getWelshRelationshipTermByGender(null);
        assertThat(welshMaleGender, nullValue());
    }

    @Test
    public void getTestWelshEmptyRelationshipTermByGender() {
        String welshMaleGender = CaseDataUtils.getWelshRelationshipTermByGender("");
        assertThat(welshMaleGender, nullValue());
    }
}
