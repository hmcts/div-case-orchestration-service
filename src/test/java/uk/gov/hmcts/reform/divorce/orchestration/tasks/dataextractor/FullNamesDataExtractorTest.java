package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_NAME;

public class FullNamesDataExtractorTest {

    @Test
    public void getPetitionerFullNameReturnsValidStringWhenAllFieldsPopulated() {
        Map<String, Object> caseData = buildCaseDataWithPetitionerNames();

        assertThat(FullNamesDataExtractor.getPetitionerFullName(caseData), is(TEST_PETITIONER_FULL_NAME));
    }

    @Test
    public void getPetitionerSolicitorFullNameReturnsValidStringWhensPopulated() {
        Map<String, Object> caseData = new HashMap<>(ImmutableMap.of(PETITIONER_SOLICITOR_NAME, "John Smith"));

        assertThat(FullNamesDataExtractor.getPetitionerSolicitorFullName(caseData), is("John Smith"));
    }

    @Test
    public void getPetitionerSolicitorFullNameReturnsEmptyString() {
        asList("", "      ", null).forEach(value -> {
            Map<String, Object> caseData = buildCaseDataWithField(PETITIONER_SOLICITOR_NAME, value);

            assertThat(FullNamesDataExtractor.getPetitionerSolicitorFullName(caseData), is(""));
        });
    }

    @Test
    public void getRespondentFullNameReturnsValidStringWhenAllFieldsPopulated() {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames();

        assertThat(FullNamesDataExtractor.getRespondentFullName(caseData), is(TEST_RESPONDENT_FULL_NAME));
    }

    @Test
    public void getRespondentSolicitorFullNameReturnsValidStringWhensPopulated() {
        Map<String, Object> caseData = new HashMap<>(ImmutableMap.of(RESPONDENT_SOLICITOR_NAME, "John Smith"));

        assertThat(FullNamesDataExtractor.getRespondentSolicitorFullName(caseData), is("John Smith"));
    }

    @Test
    public void getRespondentSolicitorFullNameReturnsEmptyString() {
        asList("", "      ", null).forEach(value -> {
            Map<String, Object> caseData = buildCaseDataWithField(RESPONDENT_SOLICITOR_NAME, value);

            assertThat(FullNamesDataExtractor.getRespondentSolicitorFullName(caseData), is(""));
        });
    }

    @Test
    public void getCoRespondentFullNameReturnsValidStringWhenAllFieldsPopulated() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentNames();

        assertThat(FullNamesDataExtractor.getCoRespondentFullName(caseData), is(TEST_CO_RESPONDENT_FULL_NAME));
    }

    @Test
    public void getCoRespondentSolicitorFullNameReturnsValidStringWhensPopulated() {
        Map<String, Object> caseData = new HashMap<>(ImmutableMap.of(CO_RESPONDENT_SOLICITOR_NAME, "John Smith"));

        assertThat(FullNamesDataExtractor.getCoRespondentSolicitorFullName(caseData), is("John Smith"));
    }

    @Test
    public void getCoRespondentSolicitorFullNameReturnsEmptyString() {
        asList("", "      ", null).forEach(value -> {
            Map<String, Object> caseData = buildCaseDataWithField(CO_RESPONDENT_SOLICITOR_NAME, value);

            assertThat(FullNamesDataExtractor.getCoRespondentSolicitorFullName(caseData), is(""));
        });
    }

    private Map<String, Object> buildCaseDataWithField(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }

    static Map<String, Object> buildCaseDataWithPetitionerNames() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);

        return caseData;
    }

    static Map<String, Object> buildCaseDataWithRespondentNames() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        return caseData;
    }

    static Map<String, Object> buildCaseDataWithCoRespondentNames() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CO_RESPONDENT_FIRST_NAME, TEST_CO_RESPONDENT_FIRST_NAME);
        caseData.put(CO_RESPONDENT_LAST_NAME, TEST_CO_RESPONDENT_LAST_NAME);

        return caseData;
    }

    static Map<String, Object> buildCaseDataWithCoRespondentSolicitorNames(String coRespondentSolicitorName) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CO_RESPONDENT_SOLICITOR_NAME, coRespondentSolicitorName);

        return caseData;
    }

}