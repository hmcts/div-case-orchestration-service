package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.ALTERNATIVE_SERVICE_APPLICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.ALTERNATIVE_SERVICE_MEDIUM;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_ADDED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_FROM;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_REFERRAL_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_DECISION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_DECISION_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_FEE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getAlternativeMedium;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getApplicationAddedDateUnformatted;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getApplicationFrom;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getApplicationReferralDateUnformatted;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getDecision;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getDecisionDateUnformatted;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getDecisionReason;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getDetails;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getIsFeeRequired;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getPaymentType;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getReason;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getType;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.isFeeRequired;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.isReasonGeneralApplicationReferral;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.isTypeOfAlternativeServiceApplication;

public class GeneralReferralDataExtractorTest {

    public static final String VALUE = "expected data";
    public static final String DATE = "2010-20-10";

    @Test
    public void getIsFeeRequiredShouldReturnValidValue() {
        assertThat(getIsFeeRequired(ImmutableMap.of(GENERAL_REFERRAL_FEE, YES_VALUE)), is(YES_VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getIsFeeRequiredShouldThrowInvalidDataForTaskException() {
        getIsFeeRequired(emptyMap());
    }

    @Test
    public void getDecisionDateUnformattedReturnsValidValue() {
        assertThat(getDecisionDateUnformatted(ImmutableMap.of(GENERAL_REFERRAL_DECISION_DATE, DATE)), is(DATE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDecisionDateShouldThrowInvalidDataForTaskException() {
        getDecisionDateUnformatted(emptyMap());
    }

    @Test
    public void getReasonReturnsValidValue() {
        assertThat(getReason(ImmutableMap.of(GENERAL_REFERRAL_REASON, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getReasonShouldThrowInvalidDataForTaskException() {
        getReason(emptyMap());
    }

    @Test
    public void getTypeReturnsValidValue() {
        assertThat(getType(ImmutableMap.of(GENERAL_REFERRAL_TYPE, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getTypeShouldThrowInvalidDataForTaskException() {
        getType(emptyMap());
    }

    @Test
    public void getDetailsReturnsValidValue() {
        assertThat(getDetails(ImmutableMap.of(GENERAL_REFERRAL_DETAILS, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDetailsShouldThrowInvalidDataForTaskException() {
        getDetails(emptyMap());
    }

    @Test
    public void getPaymentTypeReturnsValidValue() {
        assertThat(getPaymentType(ImmutableMap.of(GENERAL_REFERRAL_PAYMENT_TYPE, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getPaymentTypeShouldThrowInvalidDataForTaskException() {
        getPaymentType(emptyMap());
    }

    @Test
    public void getDecisionReturnsValidValue() {
        assertThat(getDecision(ImmutableMap.of(GENERAL_REFERRAL_DECISION, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDecisionShouldThrowInvalidDataForTaskException() {
        getDecision(emptyMap());
    }

    @Test
    public void getDecisionReasonReturnsValidValue() {
        assertThat(getDecisionReason(ImmutableMap.of(GENERAL_REFERRAL_DECISION_REASON, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDecisionReasonShouldThrowInvalidDataForTaskException() {
        getDecisionReason(emptyMap());
    }

    @Test
    public void getApplicationAddedDateUnformattedReturnsValidValue() {
        assertThat(getApplicationAddedDateUnformatted(ImmutableMap.of(GENERAL_APPLICATION_ADDED_DATE, DATE)), is(DATE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getApplicationAddedDateUnformattedShouldThrowInvalidDataForTaskException() {
        getApplicationAddedDateUnformatted(emptyMap());
    }

    @Test
    public void getApplicationFromReturnsValidValue() {
        assertThat(getApplicationFrom(ImmutableMap.of(GENERAL_APPLICATION_FROM, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getApplicationFromShouldThrowInvalidDataForTaskException() {
        getApplicationFrom(emptyMap());
    }

    @Test
    public void getApplicationReferralDateUnformattedReturnsValidValue() {
        assertThat(getApplicationReferralDateUnformatted(ImmutableMap.of(GENERAL_APPLICATION_REFERRAL_DATE, DATE)), is(DATE));
        assertThat(getApplicationReferralDateUnformatted(emptyMap()), is(""));
    }

    @Test
    public void getAlternativeMediumReturnsValidValue() {
        assertThat(getAlternativeMedium(ImmutableMap.of(ALTERNATIVE_SERVICE_MEDIUM, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getAlternativeMediumShouldThrowInvalidDataForTaskException() {
        getAlternativeMedium(emptyMap());
    }

    @Test
    public void isFeeRequiredShouldReturnValidValue() {
        assertThat(isFeeRequired(ImmutableMap.of(GENERAL_REFERRAL_FEE, YES_VALUE)), is(true));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void isFeeRequiredShouldThrowInvalidDataForTaskException() {
        isFeeRequired(emptyMap());
    }

    @Test
    public void isReasonGeneralApplicationReferralShouldReturnValidValue() {
        assertThat(isReasonGeneralApplicationReferral(ImmutableMap.of(GENERAL_REFERRAL_REASON, GENERAL_APPLICATION_REFERRAL)), is(true));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void isReasonGeneralApplicationReferralShouldThrowInvalidDataForTaskException() {
        isReasonGeneralApplicationReferral(emptyMap());
    }

    @Test
    public void isTypeOfAlternativeServiceApplicationShouldReturnValidValue() {
        assertThat(isTypeOfAlternativeServiceApplication(ImmutableMap.of(GENERAL_REFERRAL_TYPE, ALTERNATIVE_SERVICE_APPLICATION)), is(true));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void isTypeOfAlternativeServiceApplicationShouldThrowInvalidDataForTaskException() {
        isTypeOfAlternativeServiceApplication(emptyMap());
    }
}