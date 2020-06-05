package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED_OLD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;

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
    public void shouldReturnAdequateResultsFor_isAdulteryCaseWithCoRespondent() {
        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, DESERTION
        )), is(false));

        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, ADULTERY
        )), is(false));

        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, ADULTERY,
            D_8_CO_RESPONDENT_NAMED_OLD, NO_VALUE
        )), is(false));

        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, ADULTERY,
            D_8_CO_RESPONDENT_NAMED, NO_VALUE
        )), is(false));

        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, ADULTERY,
            D_8_CO_RESPONDENT_NAMED_OLD, YES_VALUE
        )), is(true));

        assertThat(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(ImmutableMap.of(
            D_8_REASON_FOR_DIVORCE, ADULTERY,
            D_8_CO_RESPONDENT_NAMED, YES_VALUE
        )), is(true));
    }

    @Test
    public void ensureDocumentIsRemovedByDocumentType() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8DOCUMENTS_GENERATED, asList(createCollectionMemberDocumentAsMap("testUrl", "myDocType", "filename")));

        Map<String, Object> returnedCaseData = CaseDataUtils.removeDocumentByDocumentType(caseData, "myDocType");

        assertThat(returnedCaseData.get(D8DOCUMENTS_GENERATED), is(emptyList()));
    }

    @Test
    public void ensureDocumentIsNotRemovedByDifferentDocumentType() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8DOCUMENTS_GENERATED, asList(
            createCollectionMemberDocumentAsMap("testUrl", "myDocType", "filename"),
            createCollectionMemberDocumentAsMap("testUrl", "myDocTypeY", "filename")
            ));

        Map<String, Object> returnedCaseData = CaseDataUtils.removeDocumentByDocumentType(caseData, "myDocType");

        List<Map<String, Object>> documents = (List) returnedCaseData.get(D8DOCUMENTS_GENERATED);
        Map<String, Object> remainingDocument = (Map<String, Object>) documents.get(0).get(VALUE_KEY);

        assertThat(documents, hasSize(1));
        assertThat(remainingDocument.get(DOCUMENT_TYPE_JSON_KEY),  is("myDocTypeY"));
    }

    @Test
    public void ensureDocumentIsNotRemovedByDifferentDocumentTypeWhenMany() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8DOCUMENTS_GENERATED, asList(
            createCollectionMemberDocumentAsMap("testUrl", "myDocTypeY", "filename"),
            createCollectionMemberDocumentAsMap("testUrl", "myDocType", "filename"),
            createCollectionMemberDocumentAsMap("testUrl", "myDocTypeZ", "filename")
        ));

        Map<String, Object> returnedCaseData = CaseDataUtils.removeDocumentByDocumentType(caseData, "myDocType");

        List<Map<String, Object>> documents = (List) returnedCaseData.get(D8DOCUMENTS_GENERATED);
        Map<String, Object> firstDocument = (Map<String, Object>) documents.get(0).get(VALUE_KEY);
        Map<String, Object> secondDocument = (Map<String, Object>) documents.get(1).get(VALUE_KEY);

        assertThat(documents, hasSize(2));
        assertThat(firstDocument.get(DOCUMENT_TYPE_JSON_KEY),  is("myDocTypeY"));
        assertThat(secondDocument.get(DOCUMENT_TYPE_JSON_KEY),  is("myDocTypeZ"));
    }

}
