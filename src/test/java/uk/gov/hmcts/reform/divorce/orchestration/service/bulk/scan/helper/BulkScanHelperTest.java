package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.hmcts.reform.bsp.common.error.FormFieldValidationException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.Month.FEBRUARY;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper.transformDateFromComponentsToCcdDate;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper.validateDateComponents;

public class BulkScanHelperTest {

    private static final String dayKey = "D8MarriageDateDay";
    private static final String monthKey = "D8MarriageDateMonth";
    private static final String yearKey = "D8MarriageDateYear";

    private static final String INVALID_DATE_ERROR_MESSAGE =
        String.format("Invalid date made up of %s, %s, %s", dayKey, monthKey, yearKey);

    private static final String UNPARSABLE_CHARACTERS_ERROR_MESSAGE =
        String.format("One or more of %s, %s, %s contain invalid characters that can't be converted into a date",
            dayKey, monthKey, yearKey);

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldTransformDateWithRightLeapYearDate() {
        LocalDate date = BulkScanHelper.transformFormDateIntoLocalDate("DateFieldName", "29/02/2020");

        assertThat(date.getDayOfMonth(), is(29));
        assertThat(date.getMonth(), is(FEBRUARY));
        assertThat(date.getYear(), is(2020));
    }

    @Test
    public void shouldFailDateTransformationWithWrongLeapYearDate() {
        expectedException.expect(FormFieldValidationException.class);
        expectedException.expectMessage("DateFieldName must be a valid date");

        BulkScanHelper.transformFormDateIntoLocalDate("DateFieldName", "29/02/2019");
    }

    @Test
    public void shouldFailIfValidateDateComponentsWithNonParsableCharacters() {
        Map<String, String> invalidDayAndMonthFieldsMap = new HashMap<String, String>() {{
                put(dayKey, "1 4");
                put(monthKey, "o1");
                put(yearKey, "1900");
            }};

        Map<String, String> invalidDayMonthYearFieldsMap = new HashMap<String, String>() {{
                put(dayKey, "07th");
                put(monthKey, "September");
                put(yearKey, "200O");
            }};

        List<Map<String, String>> listOfNonParsableDateComponents =
            asList(invalidDayAndMonthFieldsMap, invalidDayMonthYearFieldsMap);

        for (Map<String, String> invalidDateFieldMap : listOfNonParsableDateComponents) {
            List<String> validationResult = validateDateComponents(invalidDateFieldMap, dayKey, monthKey, yearKey);

            assertThat(validationResult, hasItem(UNPARSABLE_CHARACTERS_ERROR_MESSAGE));
            assertThat(validationResult.size(), is(1));
        }
    }

    @Test
    public void shouldFailIfValidateDateComponentsWithInvalidDayYear() {
        Map<String, String> invalidDayAndYearFieldsMap = new HashMap<String, String>() {{
                put(dayKey, "32");
                put(monthKey, "05");
                put(yearKey, "1899");
            }};

        List<String> validationResult = validateDateComponents(invalidDayAndYearFieldsMap, dayKey, monthKey, yearKey);

        assertThat(validationResult, hasItems(
            String.format("%s is invalid", dayKey),
            String.format("%s needs to be 4 digits e.g. 2011", yearKey),
            INVALID_DATE_ERROR_MESSAGE
        ));
        assertThat(validationResult.size(), is(3));
    }

    @Test
    public void shouldFailIfValidateDateComponentsWithInvalidMonth() {
        Map<String, String> invalidMonthFieldsMap = new HashMap<String, String>() {{
                put(dayKey, "07");
                put(monthKey, "00");
                put(yearKey, "1909");
            }};

        List<String> validationResult = validateDateComponents(invalidMonthFieldsMap, dayKey, monthKey, yearKey);

        assertThat(validationResult, hasItems(
            String.format("%s is not a valid month e.g. 03 for March", monthKey),
            INVALID_DATE_ERROR_MESSAGE
        ));
        assertThat(validationResult.size(), is(2));
    }

    @Test
    public void shouldFailIfValidateDateComponentsWithInvalidLeapDate() {
        Map<String, String> invalidLeapDateFieldsMap = new HashMap<String, String>() {{
                put(dayKey, "29");
                put(monthKey, "2");
                put(yearKey, "2019");
            }};

        List<String> validationResult = validateDateComponents(invalidLeapDateFieldsMap, dayKey, monthKey, yearKey);

        assertThat(validationResult, hasItems(
            INVALID_DATE_ERROR_MESSAGE
        ));
        assertThat(validationResult.size(), is(1));
    }

    @Test
    public void shouldPassValidateDateComponentsWithLeadingZeros() {
        Map<String, String> validDateWithLeadingZerosFieldsMap = new HashMap<String, String>() {{
                put(dayKey, "01");
                put(monthKey, "09");
                put(yearKey, "1996");
            }};

        List<String> validationResult =
            validateDateComponents(validDateWithLeadingZerosFieldsMap, dayKey, monthKey, yearKey);

        assertThat(validationResult, not(hasItems(INVALID_DATE_ERROR_MESSAGE)));
        assertThat(validationResult.size(), is(0));
    }

    @Test
    public void shouldPassValidateDateComponentsWithoutLeadingZeros() {
        Map<String, String> validDateFieldsMap = new HashMap<String, String>() {{
                put(dayKey, "30");
                put(monthKey, "11");
                put(yearKey, "2008");
            }};

        List<String> validationResult = validateDateComponents(validDateFieldsMap, dayKey, monthKey, yearKey);

        assertThat(validationResult, not(hasItems(INVALID_DATE_ERROR_MESSAGE)));
        assertThat(validationResult.size(), is(0));
    }

    @Test
    public void shouldPassValidateDateComponentsWithValidLeapDate() {
        Map<String, String> validLeapDateFieldsMap = new HashMap<String, String>() {{
                put(dayKey, "29");
                put(monthKey, "02");
                put(yearKey, "2016");
            }};

        List<String> validationResult = validateDateComponents(validLeapDateFieldsMap, dayKey, monthKey, yearKey);

        assertThat(validationResult, not(hasItems(INVALID_DATE_ERROR_MESSAGE)));
        assertThat(validationResult.size(), is(0));
    }

    @Test
    public void shouldTransformDateToCCDFormatCorrectlyGivenValidComponents() {
        String transformedDate = transformDateFromComponentsToCcdDate("02", "10", "1987");
        assertThat(transformedDate, is("1987-10-02"));
    }

    @Test
    public void shouldTransformDateToCCDFormatCorrectlyGivenValidComponentsNoLeadingZeros() {
        String transformedDate = transformDateFromComponentsToCcdDate("4", "8", "2009");
        assertThat(transformedDate, is("2009-08-04"));
    }
}