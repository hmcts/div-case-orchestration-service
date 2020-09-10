package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderDraftTaskTest.getDocumentLink;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

@RunWith(MockitoJUnitRunner.class)
public class GeneralOrderDraftRemovalTest {

    @InjectMocks
    private GeneralOrderDraftFieldsRemovalTask classUnderTest;

    @Test
    public void shouldRemoveGeneralOrderDraftKeyFromCaseData() {
        Map<String, Object> caseData = new HashMap();
        caseData.put("incomingKey", "incomingValue");
        caseData.put(classUnderTest.getFieldsToRemove().get(0), getDocumentLink());

        Map<String, Object> returnedPayload = classUnderTest.execute(contextWithToken(), caseData);

        assertThat(returnedPayload, hasKey("incomingKey"));
        assertThat(returnedPayload, not(hasKey(classUnderTest.getFieldsToRemove())));
    }

    @Test
    public void getFieldToRemoveIsValid() {
        assertThat(classUnderTest.getFieldsToRemove(), is(not(empty())));
        assertThat(classUnderTest.getFieldsToRemove().get(0), is(CcdFields.GENERAL_ORDER_DRAFT));
    }
}
