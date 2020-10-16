package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_FEE;
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
}
