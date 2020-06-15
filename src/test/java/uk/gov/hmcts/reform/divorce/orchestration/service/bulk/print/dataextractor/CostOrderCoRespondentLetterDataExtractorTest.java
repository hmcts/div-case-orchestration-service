package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CostOrderCoRespondentLetterDataExtractor.CaseDataKeys.CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CostOrderCoRespondentLetterDataExtractor.CaseDataKeys.CO_RESPONDENT_LAST_NAME;


public class CostOrderCoRespondentLetterDataExtractorTest {

    private static final String FIRST_NAME = "Finn";
    private static final String LAST_NAME = "Mertens";
    private static final String EXPECTED_FULL_NAME = "Finn Mertens";

    private static final String EXPECTED_HEARING_DATE = "20 July 2020";
    private static final String HEARING_DATE_CCD_FORMAT = "2020-07-20";
    public static final String SOLICITOR_REFERENCE = "SolRef123";

    @Test
    public void getPetitionerFullNameIsValid() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentNames(FIRST_NAME, LAST_NAME);

        assertThat(FullNamesDataExtractor.getCoRespondentFullName(caseData), is(EXPECTED_FULL_NAME));
        assertThat(FullNamesDataExtractor.getCoRespondentFullName(caseData), is(EXPECTED_FULL_NAME));
    }

    @Test
    public void getPetitionerFullNameReturnsValidStringWhenFieldsMissing() {
        asList(
            asList(FIRST_NAME, "", "Finn"),
            asList(FIRST_NAME, null, "Finn"),
            asList("", "", ""),
            asList(null, "", ""),
            asList(null, null, ""),
            asList(null, LAST_NAME, "Mertens"),
            asList("", LAST_NAME, "Mertens")
        ).forEach(values -> {
            String expected = values.get(2);
            Map<String, Object> caseData = buildCaseDataWithCoRespondentNames(values.get(0), values.get(1));

            assertThat(FullNamesDataExtractor.getCoRespondentFullName(caseData), is(expected));
        });

    }

    @Test
    public void getRespondentFullNameReturnsValidStringWhenAllFieldsPopulated() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentNames(FIRST_NAME, LAST_NAME);

        assertThat(FullNamesDataExtractor.getCoRespondentFullName(caseData), is("Finn Mertens"));
    }

    @Test
    public void getHearingDateReturnsReadableFormat() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, HEARING_DATE_CCD_FORMAT);

        assertThat(CostOrderCoRespondentLetterDataExtractor.getHearingDate(caseData), is(EXPECTED_HEARING_DATE));
    }

    @Test
    public void getSolicitorReferenceWith_ValidFormat() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, SOLICITOR_REFERENCE);

        assertThat(CostOrderCoRespondentLetterDataExtractor.getSolicitorReference(caseData), is(SOLICITOR_REFERENCE));
    }

    private static Map<String, Object> buildCaseDataWithCoRespondentNames(String firstName, String lastName) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CO_RESPONDENT_FIRST_NAME, firstName);
        caseData.put(CO_RESPONDENT_LAST_NAME, lastName);

        return caseData;
    }
}
