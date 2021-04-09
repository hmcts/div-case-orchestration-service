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
public class RespondentOrganisationPolicyRemovalTaskTest {

    @InjectMocks
    private RespondentOrganisationPolicyRemovalTask classUnderTest;

    @Test
    public void shouldRemoveFieldsFromCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("incomingKey", "incomingValue");
        caseData.put(CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY, "1");

        Map<String, Object> returnedPayload = classUnderTest.execute(contextWithToken(), caseData);

        assertThat(returnedPayload, hasKey("incomingKey"));
        classUnderTest.getFieldsToRemove().forEach(field -> assertThat(returnedPayload, not(hasKey(field))));
    }

    @Test
    public void getFieldToRemoveIsValid() {
        assertThat(classUnderTest.getFieldsToRemove().size(), is(1));
        assertThat(
            classUnderTest.getFieldsToRemove().get(0),
            is(CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY)
        );
    }
}
