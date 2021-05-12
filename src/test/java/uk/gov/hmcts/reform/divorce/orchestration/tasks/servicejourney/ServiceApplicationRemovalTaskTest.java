package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

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
public class ServiceApplicationRemovalTaskTest {

    @InjectMocks
    private ServiceApplicationRemovalTask classUnderTest;

    @Test
    public void shouldRemoveServiceRefusalDraftKeyFromCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("incomingKey", "incomingValue");
        caseData.put(CcdFields.RECEIVED_SERVICE_APPLICATION_DATE, "1");
        caseData.put(CcdFields.RECEIVED_SERVICE_ADDED_DATE, "2");
        caseData.put(CcdFields.RECEIVED_SERVICE_PAYMENT_REQUIRED, "3");
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, "4");
        caseData.put(CcdFields.SERVICE_APPLICATION_PAYMENT, "5");
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, "6");
        caseData.put(CcdFields.SERVICE_APPLICATION_DECISION_DATE, "7");
        caseData.put(CcdFields.SERVICE_APPLICATION_REFUSAL_REASON, "8");

        Map<String, Object> returnedPayload = classUnderTest.execute(contextWithToken(), caseData);

        assertThat(returnedPayload, hasKey("incomingKey"));
        classUnderTest.getFieldsToRemove().forEach(field -> assertThat(returnedPayload, not(hasKey(field))));
    }

    @Test
    public void getFieldToRemoveIsValid() {
        assertThat(classUnderTest.getFieldsToRemove().size(), is(8));
        assertThat(classUnderTest.getFieldsToRemove().get(0), is(CcdFields.RECEIVED_SERVICE_APPLICATION_DATE));
        assertThat(classUnderTest.getFieldsToRemove().get(1), is(CcdFields.RECEIVED_SERVICE_ADDED_DATE));
        assertThat(classUnderTest.getFieldsToRemove().get(2), is(CcdFields.RECEIVED_SERVICE_PAYMENT_REQUIRED));
        assertThat(classUnderTest.getFieldsToRemove().get(3), is(CcdFields.SERVICE_APPLICATION_TYPE));
        assertThat(classUnderTest.getFieldsToRemove().get(4), is(CcdFields.SERVICE_APPLICATION_PAYMENT));
        assertThat(classUnderTest.getFieldsToRemove().get(5), is(CcdFields.SERVICE_APPLICATION_GRANTED));
        assertThat(classUnderTest.getFieldsToRemove().get(6), is(CcdFields.SERVICE_APPLICATION_DECISION_DATE));
        assertThat(classUnderTest.getFieldsToRemove().get(7), is(CcdFields.SERVICE_APPLICATION_REFUSAL_REASON));
    }
}