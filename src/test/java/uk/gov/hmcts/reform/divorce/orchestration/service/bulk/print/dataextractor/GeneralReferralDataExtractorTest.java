package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.core.Is;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceGeneralReferral;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
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
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getIsFeeRequired;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getListOfGeneralReferrals;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getPaymentType;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getReason;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor.getType;

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
    public void givenNoField_whenGetListOfGeneralReferrals_shouldReturnAnEmptyArray() {
        List<CollectionMember<DivorceGeneralReferral>> result = getListOfGeneralReferrals(emptyMap());

        assertThat(result, Is.is(empty()));
    }

    @Test
    public void givenFieldWithAnEmptyArray_whenGetListOfGeneralReferrals_shouldReturnEmptyArray() {
        final List<CollectionMember<DivorceGeneralReferral>> myList = emptyList();

        List<CollectionMember<DivorceGeneralReferral>> result = getListOfGeneralReferrals(ImmutableMap.of(CcdFields.GENERAL_REFERRALS, myList));

        assertThat(result, Is.is(empty()));
    }

    @Test
    public void givenFieldWithPopulatedArray_whenGetListOfGeneralReferrals_shouldReturnPopulatedArray() {
        final List<CollectionMember<DivorceGeneralReferral>> myList = asList(new CollectionMember<>());

        List<CollectionMember<DivorceGeneralReferral>> result = getListOfGeneralReferrals(ImmutableMap.of(CcdFields.GENERAL_REFERRALS, myList));

        assertThat(result.size(), Is.is(1));
        assertThat(result, Is.is(myList));
    }
}