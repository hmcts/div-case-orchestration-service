package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.FEE_ACCOUNT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.FurtherPaymentsHelper.getGeneralReferralPaymentType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.FurtherPaymentsHelper.getPaymentType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.FurtherPaymentsHelper.isFeeAccountPayment;
import static uk.gov.hmcts.reform.divorce.orchestration.util.FurtherPaymentsHelper.isHelpWithFeePayment;

public class FurtherPaymentsHelperTest {

    @Test
    public void getGeneralReferralPaymentTypeShouldBeValidValue() {
        assertThat(getGeneralReferralPaymentType(), is(GENERAL_REFERRAL_PAYMENT_TYPE));
    }

    @Test
    public void getServiceApplicationPaymentTypeShouldBeValidValue() {
        assertThat(getGeneralReferralPaymentType(), is(GENERAL_REFERRAL_PAYMENT_TYPE));
    }

    @Test
    public void shouldReturnCorrectPaymentTypeValue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(GENERAL_REFERRAL_PAYMENT_TYPE, FEE_ACCOUNT_TYPE);

        assertThat(getPaymentType(caseData, GENERAL_REFERRAL_PAYMENT_TYPE), is("feeAccount"));
    }

    @Test
    public void shouldReturnEmptyValueWhenNonExisting() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(GENERAL_REFERRAL_PAYMENT_TYPE, null);

        assertThat(getPaymentType(caseData, GENERAL_REFERRAL_PAYMENT_TYPE), emptyString());
    }

    @Test
    public void isHelpWithFeePaymentIsTrue() {
        assertThat(isHelpWithFeePayment("helpWithFees"), is(true));
    }

    @Test
    public void isHelpWithFeePaymentIsFalse() {
        assertThat(isHelpWithFeePayment("otherPayment"), is(false));
    }

    @Test
    public void isFeeAccountPaymentIsTrue() {
        assertThat(isFeeAccountPayment("feeAccount"), is(true));
    }

    @Test
    public void isFeeAccountPaymentIsFalse() {
        assertThat(isFeeAccountPayment("otherPayment"), is(false));
    }

}