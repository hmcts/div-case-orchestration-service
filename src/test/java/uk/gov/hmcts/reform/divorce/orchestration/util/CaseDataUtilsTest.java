package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED_OLD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_LA_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.DESERTION;

public class CaseDataUtilsTest {

    private static final String FIELD_NAME = "TestField";
    private static final String LINK_ID = "LinkId";

    private CaseDataUtils caseDataUtils = new CaseDataUtils();

    @Test
    public void givenDataWithCaseLink_thenReturnLinkValue() {
        Map<String, Object> caseLinkData = ImmutableMap.of(FIELD_NAME, ImmutableMap.of(CASE_REFERENCE_FIELD, LINK_ID));
        assertThat(CaseDataUtils.getCaseLinkValue(caseLinkData, FIELD_NAME), is(LINK_ID));
    }

    @Test
    public void givenFieldNoExist_whenGetCaseLinkValue_thenReturnNull() {
        Map<String, Object> caseLinkData = DUMMY_CASE_DATA;
        assertThat(CaseDataUtils.getCaseLinkValue(caseLinkData, FIELD_NAME), nullValue());
    }

    @Test
    public void whenGetFieldAsStringObjectMap_thenReturnExpectedValue() {
        Map<String, Object> input = ImmutableMap.of(FIELD_NAME, DUMMY_CASE_DATA);
        assertThat(CaseDataUtils.getFieldAsStringObjectMap(input, FIELD_NAME), is(DUMMY_CASE_DATA));
    }

    @Test
    public void givenNonExistValue_whenGetFieldAsStringObjectMap_thenReturnNull() {
        Map<String, Object> input = DUMMY_CASE_DATA;
        assertThat(CaseDataUtils.getFieldAsStringObjectMap(input, FIELD_NAME), is(nullValue()));
    }

    @Test(expected = ClassCastException.class)
    public void givenNonStringObject_whenGetFieldAsStringObjectMap_thenReturnException() {
        Map<String, Object> input = ImmutableMap.of(FIELD_NAME, LINK_ID);
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
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(LanguagePreference.WELSH));
    }

    @Test
    public void getTestLanguagePreferenceWithValue_No() {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO_VALUE);
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(LanguagePreference.ENGLISH));
    }

    @Test
    public void getTestLanguagePreferenceWithValue_NULL() {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(LANGUAGE_PREFERENCE_WELSH, null);
        LanguagePreference languagePreference = DraftDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(LanguagePreference.ENGLISH));
    }

    @Test
    public void getTestLanguagePreferenceNotSet() {
        HashMap<String, Object> caseData = new HashMap<>();
        LanguagePreference languagePreference = DraftDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(LanguagePreference.ENGLISH));
    }

    @Test
    public void getTestLanguagePreference_Null_CaseData() {
        HashMap<String, Object> caseData = null;
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(caseData);
        assertThat(languagePreference, is(LanguagePreference.ENGLISH));
    }

    @Test
    public void givenRejectRefusalReasonWithAdditionalInfo_isRejectReasonAddInfoAwaitingTranslation_returns_true() {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD, OrchestrationConstants.DN_REFUSED_REJECT_OPTION);
        caseData.put(OrchestrationConstants.REFUSAL_REJECTION_ADDITIONAL_INFO, "some additional info that requires translation");

        caseData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        assertThat(CaseDataUtils.isRejectReasonAddInfoAwaitingTranslation(caseData), is(true));
    }

    @Test
    public void givenRejectRefusalReasonWithAdditionalInfo_isAdditionalClarification_returns_true() {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD, OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE);
        caseData.put(OrchestrationConstants.REFUSAL_CLARIFICATION_ADDITIONAL_INFO, "some additional info that requires translation");

        caseData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        assertThat(CaseDataUtils.isRejectReasonAddInfoAwaitingTranslation(caseData), is(true));
    }

    @Test
    public void givenRejectRefusalReasonWithAdditionalInfo_isCostOrderGranted_returns_true() {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE);
        caseData.put(OrchestrationConstants.COSTS_ORDER_ADDITIONAL_INFO, "some additional info that requires translation");

        caseData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        assertThat(CaseDataUtils.isRejectReasonAddInfoAwaitingTranslation(caseData), is(true));
    }

    @Test
    public void givenRejectRefusalReasonWithAdditionalInfoAndWelshAddInfo_isRejectReasonAddInfoAwaitingTranslation_returns_false() {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD, OrchestrationConstants.DN_REFUSED_REJECT_OPTION);
        caseData.put(OrchestrationConstants.REFUSAL_REJECTION_ADDITIONAL_INFO, "some additional info that requires translation");
        caseData.put(OrchestrationConstants.REFUSAL_REJECTION_ADDITIONAL_INFO_WELSH, "some welsh additional info");
        caseData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        assertThat(CaseDataUtils.isRejectReasonAddInfoAwaitingTranslation(caseData), is(false));
    }

    @Test
    public void givenRejectRefusalReasonWithAdditionalInfoAndWelshAddInfo_isAdditionalClarificationAwaitingTranslation_returns_false() {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD, OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE);
        caseData.put(OrchestrationConstants.REFUSAL_CLARIFICATION_ADDITIONAL_INFO, "some additional info that requires translation");
        caseData.put(OrchestrationConstants.REFUSAL_CLARIFICATION_ADDITIONAL_INFO_WELSH, "some welsh additional info");
        caseData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        assertThat(CaseDataUtils.isRejectReasonAddInfoAwaitingTranslation(caseData), is(false));
    }

    @Test
    public void givenRejectRefusalReasonWithAdditionalInfoAndWelshAddInfo_isCostOrderGranted_returns_false() {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE);
        caseData.put(OrchestrationConstants.COSTS_ORDER_ADDITIONAL_INFO, "some additional info that requires translation");
        caseData.put(OrchestrationConstants.COSTS_ORDER_ADDITIONAL_INFO_WELSH, "some welsh additional info");
        caseData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        assertThat(CaseDataUtils.isRejectReasonAddInfoAwaitingTranslation(caseData), is(false));
    }

    @Test
    public void givenWelshDnRefusedAndNoWelshAddtionalInfo_isWelshTranslationRequiredForDnRefusal_returns_true() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().state(WELSH_LA_DECISION).caseData(caseData).build();
        assertThat(CaseDataUtils.isWelshLADecisionTranslationRequired(caseDetails), is(true));
    }

    @Test
    public void givenWelshDnRefusedAndWelshAddtionalInfo_empty_string_returns_true() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().state(DN_REFUSED).caseData(caseData).build();
        assertThat(CaseDataUtils.isWelshLADecisionTranslationRequired(caseDetails), is(false));
    }


    @Test
    public void shouldReturnAdequateResultsFor_isAdulteryCaseWithCoRespondent() {
        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, DESERTION.getValue()
        )), is(false));

        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, ADULTERY.getValue()
        )), is(false));

        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, ADULTERY.getValue(),
            D_8_CO_RESPONDENT_NAMED_OLD, NO_VALUE
        )), is(false));

        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, ADULTERY.getValue(),
            D_8_CO_RESPONDENT_NAMED, NO_VALUE
        )), is(false));

        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, ADULTERY.getValue(),
            D_8_CO_RESPONDENT_NAMED_OLD, YES_VALUE
        )), is(true));

        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, ADULTERY.getValue(),
            D_8_CO_RESPONDENT_NAMED, YES_VALUE
        )), is(true));
    }
}
