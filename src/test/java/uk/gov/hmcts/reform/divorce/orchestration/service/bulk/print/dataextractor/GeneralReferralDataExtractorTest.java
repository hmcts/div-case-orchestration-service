package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.ALTERNATIVE_SERVICE_MEDIUM;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_ADDED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_FROM;
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
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getFee;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getPaymentType;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getReason;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getType;

public class GeneralReferralDataExtractorTest {

    public static final String VALUE = "expected data";
    public static final String DATE = "2010-20-10";

    @Test
    public void getFeeShouldReturnValidValue() {
        assertThat(getFee(ImmutableMap.of(GENERAL_REFERRAL_FEE, YES_VALUE)), is(YES_VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getFeeShouldThrowInvalidDataForTaskException() {
        getFee(emptyMap());
    }

    @Test
    public void getDecisionDateUnformattedReturnValidValue() {
        assertThat(getDecisionDateUnformatted(ImmutableMap.of(GENERAL_REFERRAL_DECISION_DATE, DATE)), is(DATE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDecisionDateShouldThrowInvalidDataForTaskException() {
        getDecisionDateUnformatted(emptyMap());
    }

    @Test
    public void getReasonReturnValidValue() {
        assertThat(getReason(ImmutableMap.of(GENERAL_REFERRAL_REASON, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getReasonShouldThrowInvalidDataForTaskException() {
        getReason(emptyMap());
    }

    @Test
    public void getTypeReturnValidValue() {
        assertThat(getType(ImmutableMap.of(GENERAL_REFERRAL_TYPE, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getTypeShouldThrowInvalidDataForTaskException() {
        getType(emptyMap());
    }

    @Test
    public void getDetailsReturnValidValue() {
        assertThat(getDetails(ImmutableMap.of(GENERAL_REFERRAL_DETAILS, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDetailsShouldThrowInvalidDataForTaskException() {
        getDetails(emptyMap());
    }

    @Test
    public void getPaymentTypeReturnValidValue() {
        assertThat(getPaymentType(ImmutableMap.of(GENERAL_REFERRAL_PAYMENT_TYPE, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getPaymentTypeShouldThrowInvalidDataForTaskException() {
        getPaymentType(emptyMap());
    }

    @Test
    public void getDecisionReturnValidValue() {
        assertThat(getDecision(ImmutableMap.of(GENERAL_REFERRAL_DECISION, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDecisionShouldThrowInvalidDataForTaskException() {
        getDecision(emptyMap());
    }

    @Test
    public void getDecisionReasonReturnValidValue() {
        assertThat(getDecisionReason(ImmutableMap.of(GENERAL_REFERRAL_DECISION_REASON, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDecisionReasonShouldThrowInvalidDataForTaskException() {
        getDecisionReason(emptyMap());
    }

    @Test
    public void getApplicationAddedDateUnformattedReturnValidValue() {
        assertThat(getApplicationAddedDateUnformatted(ImmutableMap.of(GENERAL_APPLICATION_ADDED_DATE, DATE)), is(DATE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getApplicationAddedDateUnformattedShouldThrowInvalidDataForTaskException() {
        getApplicationAddedDateUnformatted(emptyMap());
    }

    @Test
    public void getApplicationFromReturnValidValue() {
        assertThat(getApplicationFrom(ImmutableMap.of(GENERAL_APPLICATION_FROM, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getApplicationFromShouldThrowInvalidDataForTaskException() {
        getApplicationFrom(emptyMap());
    }

    @Test
    public void getApplicationReferralDateUnformattedReturnValidValue() {
        assertThat(getApplicationReferralDateUnformatted(ImmutableMap.of(GENERAL_APPLICATION_REFERRAL_DATE, DATE)), is(DATE));
        assertThat(getApplicationReferralDateUnformatted(emptyMap()), is(""));
    }

    @Test
    public void getAlternativeMediumReturnValidValue() {
        assertThat(getAlternativeMedium(ImmutableMap.of(ALTERNATIVE_SERVICE_MEDIUM, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getAlternativeMediumShouldThrowInvalidDataForTaskException() {
        getAlternativeMedium(emptyMap());
    }
}