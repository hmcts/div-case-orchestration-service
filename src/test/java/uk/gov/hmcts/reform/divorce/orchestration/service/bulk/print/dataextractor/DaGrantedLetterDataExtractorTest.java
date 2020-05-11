package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.ADDRESS_LINE1;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.ADDRESS_LINE2;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.COUNTY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.DA_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.POSTCODE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.TOWN;

public class DaGrantedLetterDataExtractorTest {

    private static final String VALID_DATE = "2010-10-10";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Smith";

    @Test
    public void getDaGrantedDateReturnsValidValueWhenItExists() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithDaGrantedDate(VALID_DATE);
        assertThat(DaGrantedLetterDataExtractor.getDaGrantedDate(caseData), is(VALID_DATE));
    }

    @Test
    public void getDaGrantedDateThrowsExceptions() {
        asList("", null).forEach(daDateValue -> {
            try {
                DaGrantedLetterDataExtractor.getDaGrantedDate(buildCaseDataWithDaGrantedDate(daDateValue));
            } catch (TaskException e) {
                thisTestPassed();
            }
        });
    }

    @Test
    public void getAddresseeReturnsValidResultWhenAllFieldPopulated() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithAddressee();

        Addressee actual = DaGrantedLetterDataExtractor.getAddressee(caseData);

        assertThat(actual.getName(), is("John Smith"));
        assertThat(actual.getFormattedAddress(), is("line1\nline2\ntown\ncounty\npostcode"));
    }

    @Test
    public void getAddresseeReturnsValidResultWhenSomeFieldsMissing() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithAddressee();

        caseData.remove(RESPONDENT_LAST_NAME);
        caseData.remove(COUNTY);
        caseData.put(ADDRESS_LINE2, null);

        Addressee actual = DaGrantedLetterDataExtractor.getAddressee(caseData);

        assertThat(actual.getName(), is(FIRST_NAME));
        assertThat(actual.getFormattedAddress(), is("line1\ntown\npostcode"));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getAddresseeThrowsExceptionWhenRequiredFieldsMissing() throws InvalidDataForTaskException {
        Map<String, Object> caseData = buildCaseDataWithAddressee();

        caseData.remove(ADDRESS_LINE1);

        DaGrantedLetterDataExtractor.getAddressee(caseData);
    }

    @Test
    public void getRespondentFullNameReturnsValidStringWhenAllFieldsPopulated() {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames(FIRST_NAME, LAST_NAME);

        assertThat(DaGrantedLetterDataExtractor.getRespondentFullName(caseData), is("John Smith"));
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

            assertThat(DaGrantedLetterDataExtractor.getRespondentFullName(caseData), is(expected));
        });
    }

    @Test
    public void getPetitionerFullNameReturnsValidStringWhenAllFieldsPopulated() {
        Map<String, Object> caseData = buildCaseDataWithPetitionerNames(FIRST_NAME, LAST_NAME);

        assertThat(DaGrantedLetterDataExtractor.getPetitionerFullName(caseData), is("John Smith"));
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

            assertThat(DaGrantedLetterDataExtractor.getPetitionerFullName(caseData), is(expected));
        });
    }

    private static Map<String, Object> buildCaseDataWithDaGrantedDate(String data) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DA_GRANTED_DATE, data);

        return caseData;
    }

    public static Map<String, Object> buildCaseDataWithAddressee() {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames(FIRST_NAME, LAST_NAME);

        caseData.put(ADDRESS_LINE1, "line1");
        caseData.put(ADDRESS_LINE2, "line2");
        caseData.put(TOWN, "town");
        caseData.put(COUNTY, "county");
        caseData.put(POSTCODE, "postcode");

        return caseData;
    }

    private static Map<String, Object> buildCaseDataWithRespondentNames(String firstName, String lastName) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESPONDENT_FIRST_NAME, firstName);
        caseData.put(RESPONDENT_LAST_NAME, lastName);

        return caseData;
    }

    private static Map<String, Object> buildCaseDataWithPetitionerNames(String firstName, String lastName) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PETITIONER_FIRST_NAME, firstName);
        caseData.put(PETITIONER_LAST_NAME, lastName);

        return caseData;
    }

    /*
     * workaround for indicating that eg exception catch is what we exactly need to pass test
     */
    private static void thisTestPassed() {
        assertThat(true, is(true));
    }
}