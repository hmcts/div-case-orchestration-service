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
public class BailiffServiceApplicationRemovalTaskTest {

    @InjectMocks
    private BailiffServiceApplicationRemovalTask classUnderTest;

    @Test
    public void shouldRemoveServiceRefusalDraftKeyFromCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("incomingKey", "incomingValue");
        caseData.put(CcdFields.RECEIVED_SERVICE_APPLICATION_DATE, "1");
        caseData.put(CcdFields.RECEIVED_SERVICE_ADDED_DATE, "2");
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, "3");
        caseData.put(CcdFields.SERVICE_APPLICATION_PAYMENT, "4");
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, "5");
        caseData.put(CcdFields.BAILIFF_APPLICATION_GRANTED, "6");
        caseData.put(CcdFields.SERVICE_APPLICATION_DECISION_DATE, "7");
        caseData.put(CcdFields.SERVICE_APPLICATION_REFUSAL_REASON, "8");
        caseData.put(CcdFields.LOCAL_COURT_ADDRESS, "9");
        caseData.put(CcdFields.LOCAL_COURT_EMAIL, "a");
        caseData.put(CcdFields.CERTIFICATE_OF_SERVICE_DATE, "b");
        caseData.put(CcdFields.BAILIFF_SERVICE_SUCCESSFUL, "c");
        caseData.put(CcdFields.REASON_FAILURE_TO_SERVE, "d");

        Map<String, Object> returnedPayload = classUnderTest.execute(contextWithToken(), caseData);

        assertThat(returnedPayload, hasKey("incomingKey"));
        classUnderTest.getFieldsToRemove().forEach(field -> assertThat(returnedPayload, not(hasKey(field))));
    }

    @Test
    public void getFieldToRemoveIsValid() {
        assertThat(classUnderTest.getFieldsToRemove().size(), is(13));
        assertThat(classUnderTest.getFieldsToRemove().get(0), is(CcdFields.RECEIVED_SERVICE_APPLICATION_DATE));
        assertThat(classUnderTest.getFieldsToRemove().get(1), is(CcdFields.RECEIVED_SERVICE_ADDED_DATE));
        assertThat(classUnderTest.getFieldsToRemove().get(2), is(CcdFields.SERVICE_APPLICATION_TYPE));
        assertThat(classUnderTest.getFieldsToRemove().get(3), is(CcdFields.SERVICE_APPLICATION_PAYMENT));
        assertThat(classUnderTest.getFieldsToRemove().get(4), is(CcdFields.SERVICE_APPLICATION_GRANTED));
        assertThat(classUnderTest.getFieldsToRemove().get(5), is(CcdFields.BAILIFF_APPLICATION_GRANTED));
        assertThat(classUnderTest.getFieldsToRemove().get(6), is(CcdFields.SERVICE_APPLICATION_DECISION_DATE));
        assertThat(classUnderTest.getFieldsToRemove().get(7), is(CcdFields.SERVICE_APPLICATION_REFUSAL_REASON));
        assertThat(classUnderTest.getFieldsToRemove().get(8), is(CcdFields.LOCAL_COURT_ADDRESS));
        assertThat(classUnderTest.getFieldsToRemove().get(9), is(CcdFields.LOCAL_COURT_EMAIL));
        assertThat(classUnderTest.getFieldsToRemove().get(10), is(CcdFields.CERTIFICATE_OF_SERVICE_DATE));
        assertThat(classUnderTest.getFieldsToRemove().get(11), is(CcdFields.BAILIFF_SERVICE_SUCCESSFUL));
        assertThat(classUnderTest.getFieldsToRemove().get(12), is(CcdFields.REASON_FAILURE_TO_SERVE));
    }
}