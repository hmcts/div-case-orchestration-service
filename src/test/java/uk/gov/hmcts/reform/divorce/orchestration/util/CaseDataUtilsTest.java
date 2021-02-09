package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceGeneralReferral;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.DN_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.WELSH_LA_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADDITIONAL_INFRORMATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED_OLD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;

public class CaseDataUtilsTest {

    private static final String FIELD_NAME = "TestField";
    private static final String LINK_ID = "LinkId";

    private final CaseDataUtils caseDataUtils = new CaseDataUtils();

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
        caseData.put(OrchestrationConstants.TYPE_COSTS_DECISION_CCD_FIELD, ADDITIONAL_INFRORMATION);
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

    @Test
    public void ensureDocumentIsNotRemovedByDifferentDocumentType() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8DOCUMENTS_GENERATED, asList(
            createCollectionMemberDocumentAsMap("testUrl", "myDocType", "filename"),
            createCollectionMemberDocumentAsMap("testUrl", "myDocTypeY", "filename")
        ));

        Map<String, Object> returnedCaseData = CaseDataUtils.removeDocumentsByDocumentType(caseData, "myDocType");

        List<Map<String, Object>> documents = (List) returnedCaseData.get(D8DOCUMENTS_GENERATED);
        Map<String, Object> remainingDocument = (Map<String, Object>) documents.get(0).get(VALUE_KEY);

        assertThat(documents, hasSize(1));
        assertThat(remainingDocument.get(DOCUMENT_TYPE_JSON_KEY), is("myDocTypeY"));
    }

    @Test
    public void ensureDocumentIsNotRemovedByDifferentDocumentTypeWhenMany() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8DOCUMENTS_GENERATED, asList(
            createCollectionMemberDocumentAsMap("testUrl", "myDocTypeY", "filename"),
            createCollectionMemberDocumentAsMap("testUrl", "myDocType", "filename"),
            createCollectionMemberDocumentAsMap("testUrl", "myDocTypeZ", "filename")
        ));

        Map<String, Object> returnedCaseData = CaseDataUtils.removeDocumentsByDocumentType(caseData, "myDocType");

        List<Map<String, Object>> documents = (List) returnedCaseData.get(D8DOCUMENTS_GENERATED);
        Map<String, Object> firstDocument = (Map<String, Object>) documents.get(0).get(VALUE_KEY);
        Map<String, Object> secondDocument = (Map<String, Object>) documents.get(1).get(VALUE_KEY);

        assertThat(documents, hasSize(2));
        assertThat(firstDocument.get(DOCUMENT_TYPE_JSON_KEY), is("myDocTypeY"));
        assertThat(secondDocument.get(DOCUMENT_TYPE_JSON_KEY), is("myDocTypeZ"));
    }

    @Test
    public void ensureAllListedDocumentsAreRemoved() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8DOCUMENTS_GENERATED, asList(
            createCollectionMemberDocumentAsMap("testUrl", "myDocTypeA", "filename"),
            createCollectionMemberDocumentAsMap("testUrl", "myDocTypeB", "filename"),
            createCollectionMemberDocumentAsMap("testUrl", "myDocTypeC", "filename")
        ));

        Map<String, Object> returnedCaseData = CaseDataUtils
            .removeDocumentsByDocumentType(caseData, "myDocTypeA", "myDocTypeB", "myDocTypeF", "myDocTypeH");

        List<Map<String, Object>> documents = (List) returnedCaseData.get(D8DOCUMENTS_GENERATED);
        Map<String, Object> firstDocument = (Map<String, Object>) documents.get(0).get(VALUE_KEY);

        assertThat(documents, hasSize(1));
        assertThat(firstDocument.get(DOCUMENT_TYPE_JSON_KEY), is("myDocTypeC"));
    }

    @Test
    public void ensureDocumentIsRemovedByDocumentType() {
        Map<String, Object> caseData =
            singletonMap(D8DOCUMENTS_GENERATED, asList(createCollectionMemberDocumentAsMap("testUrl", "myDocType", "filename")));

        Map<String, Object> returnedCaseData = CaseDataUtils.removeDocumentsByDocumentType(caseData, "myDocType");

        assertThat(returnedCaseData, not(hasKey(D8DOCUMENTS_GENERATED)));
    }

    @Test
    public void ensureDocumentArrayReturnsNullWhenThereAreNoIncomingDocuments() {
        Map<String, Object> returnedCaseData = CaseDataUtils.removeDocumentsByDocumentType(emptyMap(), "myDocType");

        assertThat(returnedCaseData, not(hasKey(D8DOCUMENTS_GENERATED)));
    }

    @Test
    public void ensureDocumentArrayReturnsNull_WhenThereIsAnEmptyListOfIncomingDocuments() {
        Map<String, Object> caseData = singletonMap(D8DOCUMENTS_GENERATED, emptyList());

        Map<String, ?> returnedCaseData = CaseDataUtils.removeDocumentsByDocumentType(caseData, "myDocType");

        List<?> returnedGeneratedDocumentsList = (List<?>) returnedCaseData.get(D8DOCUMENTS_GENERATED);
        assertThat(returnedGeneratedDocumentsList, is(empty()));
    }

    @Test
    public void givenSolPaymentMethodIsPba_whenSolicitorPaymentMethodIsPba_thenReturnTrue() {
        Map<String, Object> caseData = singletonMap(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);
        assertThat(CaseDataUtils.isSolicitorPaymentMethodPba(caseData), is(true));
    }

    @Test
    public void givenSolPaymentMethodIsNotPba_whenSolicitorPaymentMethodIsPba_thenReturnFalse() {
        Map<String, Object> caseData = singletonMap(SOLICITOR_HOW_TO_PAY_JSON_KEY, "NotByAccount");
        assertThat(CaseDataUtils.isSolicitorPaymentMethodPba(caseData), is(false));
    }

    @Test
    public void givenSolPaymentMethodDoesNotExist_whenSolicitorPaymentMethodIsPba_thenReturnFalse() {
        Map<String, Object> caseData = emptyMap();
        assertThat(CaseDataUtils.isSolicitorPaymentMethodPba(caseData), is(false));
    }

    @Test
    public void givenNoField_whenGetListOfCollectionMembers_shouldReturnAnEmptyArray() {
        List<CollectionMember<DivorceGeneralReferral>> result = CaseDataUtils.getListOfCollectionMembers(CcdFields.GENERAL_REFERRALS, emptyMap());

        assertThat(result, is(empty()));
    }

    @Test
    public void givenFieldWithAnEmptyArray_whenGetListOfCollectionMembers_shouldReturnEmptyArray() {
        final List<CollectionMember<DivorceGeneralReferral>> myList = emptyList();

        List<CollectionMember<DivorceGeneralReferral>> result = CaseDataUtils
            .getListOfCollectionMembers(CcdFields.GENERAL_REFERRALS, ImmutableMap.of(CcdFields.GENERAL_REFERRALS, myList));

        assertThat(result, is(empty()));
    }

    @Test
    public void givenFieldWithPopulatedArray_whenGetListOfCollectionMembers_shouldReturnPopulatedArray() {
        final List<CollectionMember<DivorceGeneralReferral>> myList = asList(new CollectionMember<>());

        List<CollectionMember<DivorceGeneralReferral>> result = CaseDataUtils
            .getListOfCollectionMembers(CcdFields.GENERAL_REFERRALS, ImmutableMap.of(CcdFields.GENERAL_REFERRALS, myList));

        assertThat(result.size(), is(1));
        assertThat(result, is(myList));
    }

}
