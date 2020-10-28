package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralFieldsRemovalTaskTest {

    @InjectMocks
    private GeneralReferralFieldsRemovalTask classUnderTest;

    @Test
    public void shouldRemoveGeneralReferralDraftKeysFromCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("incomingKey", "incomingValue");
        for (int i = 0; i <= 12; i++) {
            caseData.put(classUnderTest.getFieldsToRemove().get(i), "element-" + i);
        }

        Map<String, Object> returnedPayload = classUnderTest.execute(context(), caseData);

        assertThat(returnedPayload.size(), is(1));
        assertThat(returnedPayload, hasEntry("incomingKey", "incomingValue"));
        assertNoFieldsToRemoveIn(returnedPayload);
    }

    @Test
    public void getFieldToRemoveIsValid() {
        List<String> expectedFieldsToRemove = asList(
            CcdFields.GENERAL_REFERRAL_FEE,
            CcdFields.GENERAL_REFERRAL_DECISION_DATE,
            CcdFields.GENERAL_REFERRAL_REASON,
            CcdFields.GENERAL_APPLICATION_ADDED_DATE,
            CcdFields.GENERAL_APPLICATION_FROM,
            CcdFields.GENERAL_APPLICATION_REFERRAL_DATE,
            CcdFields.GENERAL_REFERRAL_TYPE,
            CcdFields.GENERAL_REFERRAL_DETAILS,
            CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE,
            CcdFields.GENERAL_REFERRAL_DECISION,
            CcdFields.GENERAL_REFERRAL_DECISION_REASON,
            CcdFields.ALTERNATIVE_SERVICE_MEDIUM,
            CcdFields.FEE_AMOUNT_WITHOUT_NOTICE
        );
        assertThat(classUnderTest.getFieldsToRemove(), is(expectedFieldsToRemove));
    }

    private void assertNoFieldsToRemoveIn(Map<String, Object> payload) {
        for (String fieldToRemove: classUnderTest.getFieldsToRemove()) {
            assertThat(payload, not(hasKey(fieldToRemove)));
        }
    }
}
