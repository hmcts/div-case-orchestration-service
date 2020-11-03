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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.ALTERNATIVE_SERVICE_APPLICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_DECISION_REFUSE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_FEE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isGeneralReferralPaymentRequired;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isGeneralReferralRejected;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isReasonGeneralApplicationReferral;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isTypeOfAlternativeServiceApplication;

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

    @Test
    public void isReasonGeneralApplicationReferralShouldBeTrue() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_REASON, GENERAL_APPLICATION_REFERRAL);

        assertThat(isReasonGeneralApplicationReferral(caseData), is(true));
    }

    @Test
    public void isReasonGeneralApplicationReferralShouldBeFalse() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_REASON, "NotGeneralApplicationReferral");

        assertThat(isReasonGeneralApplicationReferral(caseData), is(false));
    }

    @Test
    public void shouldThrowErrorWhenNoGeneralReferralReasonExists() {
        Map<String, Object> caseData = ImmutableMap.of("SomeOtherProperty", "SomeOtherValue");

        runEmptyOrNullAssertionsForGeneralReferralReason(caseData);
    }

    @Test
    public void shouldThrowErrorWhenGeneralReferralReasonIsEmpty() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_REASON, EMPTY_STRING);

        runEmptyOrNullAssertionsForGeneralReferralReason(caseData);
    }

    @Test
    public void isTypeOfAlternativeServiceApplicationShouldBeTrue() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_TYPE, ALTERNATIVE_SERVICE_APPLICATION);

        assertThat(isTypeOfAlternativeServiceApplication(caseData), is(true));
    }

    @Test
    public void isTypeOfAlternativeServiceApplicationShouldBeFalse() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_TYPE, "NotAlternativeServiceApplication");

        assertThat(isTypeOfAlternativeServiceApplication(caseData), is(false));
    }

    @Test
    public void shouldThrowErrorWhenNoGeneralReferralTypeExists() {
        Map<String, Object> caseData = ImmutableMap.of("SomeOtherProperty", "SomeOtherValue");

        runEmptyOrNullAssertionsForGeneralReferralType(caseData);
    }

    @Test
    public void shouldThrowErrorWhenGeneralReferralTypeIsEmpty() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_TYPE, EMPTY_STRING);

        runEmptyOrNullAssertionsForGeneralReferralType(caseData);
    }

    @Test
    public void isGeneralReferralRejectedShouldBeTrue() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_DECISION, GENERAL_REFERRAL_DECISION_REFUSE);

        assertThat(isGeneralReferralRejected(caseData), is(true));
    }

    @Test
    public void isGeneralReferralRejectedShouldBeFalse() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_DECISION, "NotRefuse");

        assertThat(isGeneralReferralRejected(caseData), is(false));
    }

    @Test
    public void shouldThrowErrorWhenNoGeneralReferralDecisionExists() {
        Map<String, Object> caseData = ImmutableMap.of("SomeOtherProperty", "SomeOtherValue");

        runEmptyOrNullAssertionsForGeneralReferralDecision(caseData);
    }

    @Test
    public void shouldThrowErrorWhenGeneralReferralDecisionIsEmpty() {
        Map<String, Object> caseData = ImmutableMap.of(GENERAL_REFERRAL_DECISION, EMPTY_STRING);

        runEmptyOrNullAssertionsForGeneralReferralDecision(caseData);
    }


    private void runEmptyOrNullAssertionsForGeneralReferralFee(Map<String, Object> caseData) {
        String expectedMessage = getMissingPropertyErrorMessage(GENERAL_REFERRAL_FEE);

        InvalidDataForTaskException taskException =
            assertThrows(InvalidDataForTaskException.class, () -> isGeneralReferralPaymentRequired(caseData));

        assertThat(taskException.getMessage(), containsString(expectedMessage));
    }

    private void runEmptyOrNullAssertionsForGeneralReferralReason(Map<String, Object> caseData) {
        String expectedMessage = getMissingPropertyErrorMessage(GENERAL_REFERRAL_REASON);

        InvalidDataForTaskException taskException =
            assertThrows(InvalidDataForTaskException.class, () -> isReasonGeneralApplicationReferral(caseData));

        assertThat(taskException.getMessage(), containsString(expectedMessage));
    }

    private void runEmptyOrNullAssertionsForGeneralReferralType(Map<String, Object> caseData) {
        String expectedMessage = getMissingPropertyErrorMessage(GENERAL_REFERRAL_TYPE);

        InvalidDataForTaskException taskException =
            assertThrows(InvalidDataForTaskException.class, () -> isTypeOfAlternativeServiceApplication(caseData));

        assertThat(taskException.getMessage(), containsString(expectedMessage));
    }

    private void runEmptyOrNullAssertionsForGeneralReferralDecision(Map<String, Object> caseData) {
        String expectedMessage = getMissingPropertyErrorMessage(GENERAL_REFERRAL_DECISION);

        InvalidDataForTaskException taskException =
            assertThrows(InvalidDataForTaskException.class, () -> isGeneralReferralRejected(caseData));

        assertThat(taskException.getMessage(), containsString(expectedMessage));
    }

    private String getMissingPropertyErrorMessage(String missingField) {
        return format("Could not evaluate value of mandatory property \"%s\"", missingField);
    }
}
