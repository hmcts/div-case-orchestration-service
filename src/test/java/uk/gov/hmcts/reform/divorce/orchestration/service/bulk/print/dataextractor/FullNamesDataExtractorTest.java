package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_NAME;

public class FullNamesDataExtractorTest {

    static final String FIRST_NAME = "John";
    static final String LAST_NAME = "Smith";
    static final String CO_REP_FIRST_NAME = "Jane";
    static final String CO_REP_LAST_NAME = "Sam";

    @Test
    public void getPetitionerFullNameReturnsValidStringWhenAllFieldsPopulated() {
        Map<String, Object> caseData = buildCaseDataWithPetitionerNames(FIRST_NAME, LAST_NAME);

        Assert.assertThat(FullNamesDataExtractor.getPetitionerFullName(caseData), is("John Smith"));
    }

    @Test
    public void getPetitionerSolicitorFullNameReturnsValidStringWhensPopulated() {
        Map<String, Object> caseData = new HashMap<>(ImmutableMap.of(PETITIONER_SOLICITOR_NAME, "John Smith"));

        Assert.assertThat(FullNamesDataExtractor.getPetitionerSolicitorFullName(caseData), is("John Smith"));
    }

    @Test
    public void getPetitionerSolicitorFullNameReturnsEmptyString() {
        asList("", "      ", null).forEach(value -> {
            Map<String, Object> caseData = buildCaseDataWithField(PETITIONER_SOLICITOR_NAME, value);

            assertThat(FullNamesDataExtractor.getPetitionerSolicitorFullName(caseData), is(""));
        });
    }

    @Test
    public void getPetitionerFullNameReturnsValidStringWhenFieldsMissing() {
        // fistName, lastName, expected
        asList(
            asList(FIRST_NAME, "", "John"),
            asList(FIRST_NAME, null, "John"),
            asList("", "", ""),
            asList(null, "", ""),
            asList(null, null, ""),
            asList(null, LAST_NAME, "Smith"),
            asList("", LAST_NAME, "Smith")
        ).forEach(values -> {
            String expected = values.get(2);
            Map<String, Object> caseData = buildCaseDataWithPetitionerNames(values.get(0), values.get(1));

            Assert.assertThat(FullNamesDataExtractor.getPetitionerFullName(caseData), is(expected));
        });
    }

    @Test
    public void getRespondentFullNameReturnsValidStringWhenAllFieldsPopulated() {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames(FIRST_NAME, LAST_NAME);

        assertThat(FullNamesDataExtractor.getRespondentFullName(caseData), is("John Smith"));
    }

    @Test
    public void getRespondentFullNameReturnsValidStringWhenFieldsMissing() {
        // fistName, lastName, expected
        asList(
            asList(FIRST_NAME, "", "John"),
            asList(FIRST_NAME, null, "John"),
            asList("", "", ""),
            asList(null, "", ""),
            asList(null, null, ""),
            asList(null, LAST_NAME, "Smith"),
            asList("", LAST_NAME, "Smith")
        ).forEach(values -> {
            String expected = values.get(2);
            Map<String, Object> caseData = buildCaseDataWithRespondentNames(values.get(0), values.get(1));

            assertThat(FullNamesDataExtractor.getRespondentFullName(caseData), is(expected));
        });
    }

    @Test
    public void getRespondentSolicitorFullNameReturnsValidStringWhensPopulated() {
        Map<String, Object> caseData = new HashMap<>(ImmutableMap.of(RESPONDENT_SOLICITOR_NAME, "John Smith"));

        Assert.assertThat(FullNamesDataExtractor.getRespondentSolicitorFullName(caseData), is("John Smith"));
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

        assertThat(FullNamesDataExtractor.getCoRespondentFullName(caseData), is("Jane Sam"));
    }

    @Test
    public void getCoRespondentSolicitorFullNameReturnsValidStringWhensPopulated() {
        Map<String, Object> caseData = new HashMap<>(ImmutableMap.of(CO_RESPONDENT_SOLICITOR_NAME, "John Smith"));

        Assert.assertThat(FullNamesDataExtractor.getCoRespondentSolicitorFullName(caseData), is("John Smith"));
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

    private static Map<String, Object> buildCaseDataWithPetitionerNames(String firstName, String lastName) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PETITIONER_FIRST_NAME, firstName);
        caseData.put(PETITIONER_LAST_NAME, lastName);

        return caseData;
    }

    static Map<String, Object> buildCaseDataWithRespondentNames(String firstName, String lastName) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESPONDENT_FIRST_NAME, firstName);
        caseData.put(RESPONDENT_LAST_NAME, lastName);

        return caseData;
    }

    static Map<String, Object> buildCaseDataWithRespondentSolicitorNames(String name) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESPONDENT_SOLICITOR_NAME, name);

        return caseData;
    }

    static Map<String, Object> buildCaseDataWithCoRespondentNames() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CO_RESPONDENT_FIRST_NAME, CO_REP_FIRST_NAME);
        caseData.put(CO_RESPONDENT_LAST_NAME, CO_REP_LAST_NAME);

        return caseData;
    }

    static Map<String, Object> buildCaseDataWithCoRespondentSolicitorNames(String coRespondentSolicitorName) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CO_RESPONDENT_SOLICITOR_NAME, coRespondentSolicitorName);

        return caseData;
    }

}