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
public class GeneralOrderFieldsRemovalTaskTest {

    @InjectMocks
    private GeneralOrderFieldsRemovalTask classUnderTest;

    @Test
    public void shouldRemoveGeneralOrderDraftKeyFromCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("incomingKey", "incomingValue");
        caseData.put(classUnderTest.getFieldsToRemove().get(0), getDocumentLink());
        caseData.put(classUnderTest.getFieldsToRemove().get(1), "a");
        caseData.put(classUnderTest.getFieldsToRemove().get(2), "b");
        caseData.put(classUnderTest.getFieldsToRemove().get(3), "c");
        caseData.put(classUnderTest.getFieldsToRemove().get(4), "d");
        caseData.put(classUnderTest.getFieldsToRemove().get(5), "e");
        caseData.put(classUnderTest.getFieldsToRemove().get(6), "f");

        Map<String, Object> returnedPayload = classUnderTest.execute(contextWithToken(), caseData);

        assertThat(returnedPayload.size(), is(1));
        assertThat(returnedPayload, hasKey("incomingKey"));
        assertThat(returnedPayload, not(hasKey(classUnderTest.getFieldsToRemove())));
    }

    @Test
    public void getFieldToRemoveIsValid() {
        assertThat(classUnderTest.getFieldsToRemove(), is(not(empty())));
        assertThat(classUnderTest.getFieldsToRemove().get(0), is(CcdFields.GENERAL_ORDER_DRAFT));
        assertThat(classUnderTest.getFieldsToRemove().get(1), is(CcdFields.GENERAL_ORDER_RECITALS));
        assertThat(classUnderTest.getFieldsToRemove().get(2), is(CcdFields.GENERAL_ORDER_PARTIES));
        assertThat(classUnderTest.getFieldsToRemove().get(3), is(CcdFields.GENERAL_ORDER_DATE));
        assertThat(classUnderTest.getFieldsToRemove().get(4), is(CcdFields.GENERAL_ORDER_DETAILS));
        assertThat(classUnderTest.getFieldsToRemove().get(5), is(CcdFields.JUDGE_NAME));
        assertThat(classUnderTest.getFieldsToRemove().get(6), is(CcdFields.JUDGE_TYPE));
    }
}
