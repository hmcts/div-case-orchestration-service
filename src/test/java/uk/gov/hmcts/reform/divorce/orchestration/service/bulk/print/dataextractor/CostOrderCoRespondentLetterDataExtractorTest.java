package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CostOrderCoRespondentLetterDataExtractor.CaseDataKeys.CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CostOrderCoRespondentLetterDataExtractor.CaseDataKeys.CO_RESPONDENT_LAST_NAME;


public class CostOrderCoRespondentLetterDataExtractorTest {

    private static final String VALID_DATE = LocalDate.now().toString();
    private static final String FIRST_NAME = "Finn";
    private static final String LAST_NAME = "Mertens";
    private static final String EXPECTED_FULL_NAME = "Finn Mertens";

    private static final String ADDRESS = "4 Green Falls\nHillway\nTownship\nCountyVille\nTW10 2AA";

    @Test
    public void getPetitionerFullNameIsValid() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentNames(FIRST_NAME, LAST_NAME);

        assertThat(CostOrderCoRespondentLetterDataExtractor.getCoRespondentFullName(caseData), is(EXPECTED_FULL_NAME));
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

            assertThat(CostOrderCoRespondentLetterDataExtractor.getCoRespondentFullName(caseData), is(expected));
        });

    }

    @Test
    public void getRespondentFullNameReturnsValidStringWhenAllFieldsPopulated() {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentNames(FIRST_NAME, LAST_NAME);

        assertThat(CostOrderCoRespondentLetterDataExtractor.getCoRespondentFullName(caseData), is("Finn Mertens"));
    }

    private static Map<String, Object> buildCaseDataWithCoRespondentNames(String firstName, String lastName) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CO_RESPONDENT_FIRST_NAME, firstName);
        caseData.put(CO_RESPONDENT_LAST_NAME, lastName);

        return caseData;
    }

//    private static Map<String, Object> buildCaseDataWithCostClaimGranted(boolean value) {
//        String costClaimGrantedYesNoValue;
//
//        costClaimGrantedYesNoValue = value ? "Yes" : "No";
//
//        Map<String, Object> caseData = new HashMap<>();
//        caseData.put(COSTS_CLAIM_GRANTED, costClaimGrantedYesNoValue);
//        return caseData;
//    }

    public static Map<String, Object> buildCaseDataWithAddressee(String addressField) {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentNames(FIRST_NAME, LAST_NAME);

        caseData.put(addressField, ADDRESS);

        return caseData;
    }


    // TODO: WIP - ADD THESE TEST CASES

    // getLetterDate

    // getCoRespondentFullName
}