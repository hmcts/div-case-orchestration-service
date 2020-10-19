package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.Map;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_FEE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isGeneralReferralPaymentRequired;

public class GeneralReferralHelperTest {

    @Test
    public void isGeneralReferralPaymentRequiredShouldBeTrue() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_FEE, YES_VALUE);

        assertThat(isGeneralReferralPaymentRequired(caseData), is(true));
    }

    @Test
    public void isGeneralReferralPaymentRequiredShouldBeFalse() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_FEE, NO_VALUE);

        assertThat(isGeneralReferralPaymentRequired(caseData), is(false));
    }

    @Test
    public void shouldThrowErrorWhenNoGeneralReferralFeeExists() {
        Map<String, Object> caseData = ImmutableMap.of("SomeOtherProperty", "SomeOtherValue");

        runEmptyOrNullAssertionsForGeneralReferralFee(caseData);
    }

    @Test
    public void shouldThrowErrorWhenGeneralReferralFeeIsEmpty() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_FEE, EMPTY_STRING);

        runEmptyOrNullAssertionsForGeneralReferralFee(caseData);
    }

    private void runEmptyOrNullAssertionsForGeneralReferralFee(Map<String, Object> caseData) {
        String expectedMessage = format("Could not evaluate value of mandatory property \"%s\"", GENERAL_REFERRAL_FEE);

        InvalidDataForTaskException taskException = assertThrows(InvalidDataForTaskException.class, () -> isGeneralReferralPaymentRequired(caseData));

        assertThat(taskException.getMessage(), containsString(expectedMessage));
    }
}
