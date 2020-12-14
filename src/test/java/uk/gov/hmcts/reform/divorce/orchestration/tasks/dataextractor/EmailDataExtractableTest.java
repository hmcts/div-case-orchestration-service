package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_OTHER_PARTY_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.CaseDataKeys.CO_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.CaseDataKeys.CO_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.CaseDataKeys.OTHER_PARTY_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.CaseDataKeys.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.CaseDataKeys.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.CaseDataKeys.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.CaseDataKeys.RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.getCoRespondentEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.getCoRespondentSolicitorEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.getOtherPartyEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.getPetitionerEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.getPetitionerEmailOrEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.getPetitionerSolicitorEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.getRespondentEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.getRespondentSolicitorEmail;

public class EmailDataExtractableTest {
    @Test
    public void getPetitionerEmailShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(PETITIONER_EMAIL, TEST_EMAIL);
        assertThat(getPetitionerEmail(caseData), is(TEST_EMAIL));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getPetitionerEmailShouldThrowInvalidDataForTaskException() {
        getPetitionerEmail(Collections.emptyMap());
    }

    @Test
    public void getPetitionerEmailShouldReturnValidValues() {
        Map<String, Object> caseData = buildCaseDataWithField(PETITIONER_EMAIL, TEST_EMAIL);

        assertThat(getPetitionerEmailOrEmpty(caseData), is(TEST_EMAIL));
    }

    @Test
    public void getPetitionerEmailOrEmptyShouldReturnEmptyWhenNoFieldGiven() {
        Map<String, Object> caseData = new HashMap<>();

        assertThat(getPetitionerEmailOrEmpty(caseData), is(""));
    }

    @Test
    public void getPetitionerEmailOrEmptyShouldReturnEmptyWhenEmpty() {
        Map<String, Object> caseData = buildCaseDataWithField(PETITIONER_EMAIL, "");

        assertThat(getPetitionerEmailOrEmpty(caseData), is(""));
    }

    @Test
    public void getPetitionerSolicitorEmailShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(PETITIONER_SOLICITOR_EMAIL, TEST_PETITIONER_EMAIL);
        assertThat(getPetitionerSolicitorEmail(caseData), is(TEST_PETITIONER_EMAIL));
    }

    @Test
    public void getRespondentEmailShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(RESPONDENT_EMAIL, TEST_RESPONDENT_EMAIL);
        assertThat(getRespondentEmail(caseData), is(TEST_RESPONDENT_EMAIL));
    }

    @Test
    public void getRespondentSolicitorEmailShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(RESPONDENT_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        assertThat(getRespondentSolicitorEmail(caseData), is(TEST_RESP_SOLICITOR_EMAIL));
    }

    @Test
    public void getCoRespondentEmailShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(CO_RESPONDENT_EMAIL, TEST_RESPONDENT_EMAIL);
        assertThat(getCoRespondentEmail(caseData), is(TEST_RESPONDENT_EMAIL));
    }

    @Test
    public void getCoRespondentSolicitorEmailShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(CO_RESPONDENT_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        assertThat(getCoRespondentSolicitorEmail(caseData), is(TEST_RESP_SOLICITOR_EMAIL));
    }

    @Test
    public void getOtherPartyEmailShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(OTHER_PARTY_EMAIL, TEST_OTHER_PARTY_EMAIL);
        assertThat(getOtherPartyEmail(caseData), is(TEST_OTHER_PARTY_EMAIL));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getPetitionerSolicitorEmailShouldThrowInvalidDataForTaskException() {
        getPetitionerSolicitorEmail(Collections.emptyMap());
    }

    private static Map<String, Object> buildCaseDataWithField(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }
}