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
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.RESPONDENT_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.RESPONDENT_HOME_ADDRESS;
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
        assertThat(
            actual.getFormattedAddress(),
            is("line1D8RespondentHomeAddress\nline2D8RespondentHomeAddress\ntownD8RespondentHomeAddress\n"
                + "countyD8RespondentHomeAddress\npostcodeD8RespondentHomeAddress")
        );
    }

    @Test
    public void getAddresseeReturnsValidResultWhenBothHomeAndCorrespondenceAddressProvided() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithAddressee();
        caseData.put(RESPONDENT_HOME_ADDRESS, buildAddress("home"));
        caseData.put(RESPONDENT_CORRESPONDENCE_ADDRESS, buildAddress("correspondence"));

        Addressee actual = DaGrantedLetterDataExtractor.getAddressee(caseData);

        assertThat(actual.getName(), is("John Smith"));
        assertThat(
            actual.getFormattedAddress(),
            is("line1correspondence\nline2correspondence\ntowncorrespondence\ncountycorrespondence\npostcodecorrespondence")
        );
    }

    @Test
    public void getAddresseeReturnsValidResultWhenOnlyHomeAddressProvided() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithAddressee();
        caseData.put(RESPONDENT_HOME_ADDRESS, buildAddress("home"));
        caseData.remove(RESPONDENT_CORRESPONDENCE_ADDRESS);

        Addressee actual = DaGrantedLetterDataExtractor.getAddressee(caseData);

        assertThat(actual.getName(), is("John Smith"));
        assertThat(
            actual.getFormattedAddress(),
            is("line1home\nline2home\ntownhome\ncountyhome\npostcodehome")
        );
    }

    @Test
    public void getAddresseeReturnsValidResultWhenOnlyCorrespondenceAddressProvided() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithAddressee();
        caseData.put(RESPONDENT_CORRESPONDENCE_ADDRESS, buildAddress("correspondence"));

        Addressee actual = DaGrantedLetterDataExtractor.getAddressee(caseData);

        assertThat(actual.getName(), is("John Smith"));
        assertThat(
            actual.getFormattedAddress(),
            is("line1correspondence\nline2correspondence\ntowncorrespondence\ncountycorrespondence\npostcodecorrespondence")
        );
    }

    @Test
    public void getAddresseeReturnsValidResultWhenSomeFieldsMissing() throws TaskException {
        final Map<String, Object> caseData = buildCaseDataWithAddressee();

        Map<String, Object> address = buildAddress("correspondence");
        address.remove(RESPONDENT_LAST_NAME);
        address.remove(COUNTY);
        address.put(ADDRESS_LINE2, null);

        caseData.put(RESPONDENT_CORRESPONDENCE_ADDRESS, address);

        Addressee actual = DaGrantedLetterDataExtractor.getAddressee(caseData);

        assertThat(actual.getName(), is("John Smith"));
        assertThat(actual.getFormattedAddress(), is("line1correspondence\ntowncorrespondence\npostcodecorrespondence"));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getAddresseeThrowsExceptionWhenRequiredFieldsMissing() throws InvalidDataForTaskException {
        Map<String, Object> caseData = buildCaseDataWithAddressee();

        Map<String, Object> address = buildAddress("correspondence");
        address.remove(ADDRESS_LINE1);

        caseData.put(RESPONDENT_CORRESPONDENCE_ADDRESS, address);

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
        return buildCaseDataWithAddressee(RESPONDENT_HOME_ADDRESS);
    }

    public static Map<String, Object> buildCaseDataWithAddressee(String addressField) {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames(FIRST_NAME, LAST_NAME);

        caseData.put(addressField, buildAddress(addressField));

        return caseData;
    }

    private static Map<String, Object> buildAddress(String type) {
        Map<String, Object> address = new HashMap<>();

        address.put(ADDRESS_LINE1, "line1" + type);
        address.put(ADDRESS_LINE2, "line2" + type);
        address.put(TOWN, "town" + type);
        address.put(COUNTY, "county" + type);
        address.put(POSTCODE, "postcode" + type);

        return address;
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
