package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

@RunWith(MockitoJUnitRunner.class)
public class RespondentDetailsRemovalTaskTest {

    @InjectMocks
    private RespondentDetailsRemovalTask classUnderTest;

    @Test
    public void shouldRemoveFieldsFromCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("incomingKey", "incomingValue");
        caseData.put(CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY, "1");
        caseData.put(CcdFields.RESPONDENT_SOLICITOR_NAME, "1");
        caseData.put(CcdFields.RESPONDENT_SOLICITOR_REFERENCE, "1");
        caseData.put(CcdFields.RESPONDENT_SOLICITOR_PHONE, "1");
        caseData.put(CcdFields.RESPONDENT_SOLICITOR_EMAIL, "1");
        caseData.put(CcdFields.RESPONDENT_SOLICITOR_ADDRESS, "1");
        caseData.put(CcdFields.RESPONDENT_SOLICITOR_DIGITAL, "1");

        Map<String, Object> returnedPayload = classUnderTest.execute(contextWithToken(), caseData);

        assertThat(returnedPayload, hasKey("incomingKey"));
        classUnderTest.getFieldsToRemove().forEach(field -> assertThat(returnedPayload, not(hasKey(field))));
    }

    @Test
    public void getFieldToRemoveIsValid() {
        assertThat(classUnderTest.getFieldsToRemove().size(), is(7));
        assertThat(
            classUnderTest.getFieldsToRemove().get(0),
            is(CcdFields.RESPONDENT_SOLICITOR_NAME)
        );
        assertThat(
            classUnderTest.getFieldsToRemove().get(1),
            is(CcdFields.RESPONDENT_SOLICITOR_REFERENCE)
        );
        assertThat(
            classUnderTest.getFieldsToRemove().get(2),
            is(CcdFields.RESPONDENT_SOLICITOR_PHONE)
        );
        assertThat(
            classUnderTest.getFieldsToRemove().get(3),
            is(CcdFields.RESPONDENT_SOLICITOR_EMAIL)
        );
        assertThat(
            classUnderTest.getFieldsToRemove().get(4),
            is(CcdFields.RESPONDENT_SOLICITOR_ADDRESS)
        );
        assertThat(
            classUnderTest.getFieldsToRemove().get(5),
            is(CcdFields.RESPONDENT_SOLICITOR_DIGITAL)
        );
        assertThat(
            classUnderTest.getFieldsToRemove().get(6),
            is(CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY)
        );
    }
}
