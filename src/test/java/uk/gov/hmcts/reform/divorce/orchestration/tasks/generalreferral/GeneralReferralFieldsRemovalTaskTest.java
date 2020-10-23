package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

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
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralFieldsRemovalTaskTest {

    @InjectMocks
    private GeneralReferralFieldsRemovalTask classUnderTest;

    @Test
    public void shouldRemoveGeneralReferralDraftKeysFromCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("incomingKey", "incomingValue");
        caseData.put(classUnderTest.getFieldsToRemove().get(0), getDocumentLink());
        caseData.put(classUnderTest.getFieldsToRemove().get(1), "a");
        caseData.put(classUnderTest.getFieldsToRemove().get(2), "b");
        caseData.put(classUnderTest.getFieldsToRemove().get(3), "c");
        caseData.put(classUnderTest.getFieldsToRemove().get(4), "d");
        caseData.put(classUnderTest.getFieldsToRemove().get(5), "e");
        caseData.put(classUnderTest.getFieldsToRemove().get(6), "f");
        caseData.put(classUnderTest.getFieldsToRemove().get(7), "g");
        caseData.put(classUnderTest.getFieldsToRemove().get(8), "h");
        caseData.put(classUnderTest.getFieldsToRemove().get(9), "i");
        caseData.put(classUnderTest.getFieldsToRemove().get(10), "j");
        caseData.put(classUnderTest.getFieldsToRemove().get(11), "k");
        caseData.put(classUnderTest.getFieldsToRemove().get(12), "l");

        Map<String, Object> returnedPayload = classUnderTest.execute(context(), caseData);

        assertThat(returnedPayload.size(), is(1));
        assertThat(returnedPayload, hasKey("incomingKey"));
        assertThat(returnedPayload, not(hasKey(classUnderTest.getFieldsToRemove())));
    }

    @Test
    public void getFieldToRemoveIsValid() {
        assertThat(classUnderTest.getFieldsToRemove(), is(not(empty())));
        assertThat(classUnderTest.getFieldsToRemove().get(0), is(CcdFields.GENERAL_REFERRAL_FEE));
        assertThat(classUnderTest.getFieldsToRemove().get(1), is(CcdFields.GENERAL_REFERRAL_DECISION_DATE));
        assertThat(classUnderTest.getFieldsToRemove().get(2), is(CcdFields.GENERAL_REFERRAL_REASON));
        assertThat(classUnderTest.getFieldsToRemove().get(3), is(CcdFields.GENERAL_APPLICATION_ADDED_DATE));
        assertThat(classUnderTest.getFieldsToRemove().get(4), is(CcdFields.GENERAL_APPLICATION_FROM));
        assertThat(classUnderTest.getFieldsToRemove().get(5), is(CcdFields.GENERAL_APPLICATION_REFERRAL_DATE));
        assertThat(classUnderTest.getFieldsToRemove().get(6), is(CcdFields.GENERAL_REFERRAL_TYPE));
        assertThat(classUnderTest.getFieldsToRemove().get(7), is(CcdFields.GENERAL_REFERRAL_DETAILS));
        assertThat(classUnderTest.getFieldsToRemove().get(8), is(CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE));
        assertThat(classUnderTest.getFieldsToRemove().get(9), is(CcdFields.GENERAL_REFERRAL_DECISION));
        assertThat(classUnderTest.getFieldsToRemove().get(10), is(CcdFields.GENERAL_REFERRAL_DECISION_REASON));
        assertThat(classUnderTest.getFieldsToRemove().get(11), is(CcdFields.ALTERNATIVE_SERVICE_MEDIUM));
        assertThat(classUnderTest.getFieldsToRemove().get(12), is(CcdFields.FEE_AMOUNT_WITHOUT_NOTICE));
    }
}
