package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.RESPONDENT_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.buildCaseDataWithRespondentNames;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractorTest.buildCaseDataWithRespondentSolicitorNames;

public class AddresseeDataExtractorTest {

    public static String FORMATTED_ADDRESS = "this is my address\ntown\ncounty\npostcode";
    public static String EXPECTED_NAME = FIRST_NAME + " " + LAST_NAME;

    @Test
    public void getRespondentShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithRespondentAsAddressee();

        Addressee addressee = AddresseeDataExtractor.getRespondent(caseData);

        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is(EXPECTED_NAME));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getRespondentShouldThrowInvalidDataForTaskException() {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames(FIRST_NAME, LAST_NAME);

        AddresseeDataExtractor.getRespondent(caseData);
    }

    @Test
    public void getRespondentSolicitorShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithRespondentSolicitorAsAddressee();

        Addressee addressee = AddresseeDataExtractor.getRespondentSolicitor(caseData);

        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));
        assertThat(addressee.getName(), is(EXPECTED_NAME));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getRespondentSolicitorShouldThrowInvalidDataForTaskException() {
        Map<String, Object> caseData = buildCaseDataWithRespondentSolicitorNames(EXPECTED_NAME);

        AddresseeDataExtractor.getRespondent(caseData);
    }

    public static Map<String, Object> buildCaseDataWithRespondentAsAddressee() {
        Map<String, Object> caseData = buildCaseDataWithRespondentNames(FIRST_NAME, LAST_NAME);
        caseData.put(RESPONDENT_CORRESPONDENCE_ADDRESS, FORMATTED_ADDRESS);

        return caseData;
    }

    public static Map<String, Object> buildCaseDataWithRespondentSolicitorAsAddressee() {
        Map<String, Object> caseData = buildCaseDataWithRespondentSolicitorNames(EXPECTED_NAME);
        caseData.put(RESPONDENT_SOLICITOR_ADDRESS, FORMATTED_ADDRESS);

        return caseData;
    }
}
