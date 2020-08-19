package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.PETITIONER_EMAIL;

public class CaseDataExtractorTest {

    @Test
    public void getCaseReferenceShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        assertThat(CaseDataExtractor.getCaseReference(caseData), is(TEST_CASE_FAMILY_MAN_ID));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getCaseReferenceShouldThrowInvalidDataForTaskException() {
        CaseDataExtractor.getCaseReference(Collections.emptyMap());
    }

    @Test
    public void getPetitionerEmailShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(PETITIONER_EMAIL, TEST_EMAIL);
        assertThat(CaseDataExtractor.getPetitionerEmail(caseData), is(TEST_EMAIL));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getPetitionerEmailShouldThrowInvalidDataForTaskException() {
        CaseDataExtractor.getPetitionerEmail(Collections.emptyMap());
    }

    @Test
    public void getPetitionerEmailShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithField(PETITIONER_EMAIL, TEST_EMAIL);

        assertThat(CaseDataExtractor.getPetitionerEmailOrEmpty(caseData), is(TEST_EMAIL));
    }

    @Test
    public void getPetitionerEmailOrEmptyShouldReturnEmptyWhenNoFieldGiven() {
        Map<String, Object> caseData = new HashMap<>();

        assertThat(CaseDataExtractor.getPetitionerEmailOrEmpty(caseData), is(""));
    }

    @Test
    public void getPetitionerEmailOrEmptyShouldReturnEmptyWhenNull() {
        Map<String, Object> caseData = buildCaseDataWithField(PETITIONER_EMAIL, null);

        assertThat(CaseDataExtractor.getPetitionerEmailOrEmpty(caseData), is(""));
    }

    @Test
    public void getPetitionerEmailOrEmptyShouldReturnEmptyWhenEmpty() {
        Map<String, Object> caseData = buildCaseDataWithField(PETITIONER_EMAIL, "");

        assertThat(CaseDataExtractor.getPetitionerEmailOrEmpty(caseData), is(""));
    }

    private static Map<String, Object> buildCaseDataWithField(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }
}