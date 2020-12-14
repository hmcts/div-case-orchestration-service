package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor;

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

public class GeneralReferralDataExtractableTest {

    public static final String VALUE = "expected data";
    public static final String DATE = "2010-20-10";

    private GeneralReferralDataExtractable classUnderTest = new GeneralReferralDataExtractable() {
    };

    @Test
    public void getIsFeeRequiredShouldReturnValidValue() {
        assertThat(classUnderTest.getIsFeeRequired(ImmutableMap.of(GENERAL_REFERRAL_FEE, YES_VALUE)), is(YES_VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getIsFeeRequiredShouldThrowInvalidDataForTaskException() {
        classUnderTest.getIsFeeRequired(emptyMap());
    }

    @Test
    public void getDecisionDateUnformattedReturnsValidValue() {
        assertThat(classUnderTest.getDecisionDateUnformatted(ImmutableMap.of(GENERAL_REFERRAL_DECISION_DATE, DATE)), is(DATE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDecisionDateShouldThrowInvalidDataForTaskException() {
        classUnderTest.getDecisionDateUnformatted(emptyMap());
    }

    @Test
    public void getReasonReturnsValidValue() {
        assertThat(classUnderTest.getReason(ImmutableMap.of(GENERAL_REFERRAL_REASON, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getReasonShouldThrowInvalidDataForTaskException() {
        classUnderTest.getReason(emptyMap());
    }

    @Test
    public void getTypeReturnsValidValue() {
        assertThat(classUnderTest.getType(ImmutableMap.of(GENERAL_REFERRAL_TYPE, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getTypeShouldThrowInvalidDataForTaskException() {
        classUnderTest.getType(emptyMap());
    }

    @Test
    public void getDetailsReturnsValidValue() {
        assertThat(classUnderTest.getDetails(ImmutableMap.of(GENERAL_REFERRAL_DETAILS, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDetailsShouldThrowInvalidDataForTaskException() {
        classUnderTest.getDetails(emptyMap());
    }

    @Test
    public void getPaymentTypeReturnsValidValue() {
        assertThat(classUnderTest.getPaymentType(ImmutableMap.of(GENERAL_REFERRAL_PAYMENT_TYPE, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getPaymentTypeShouldThrowInvalidDataForTaskException() {
        classUnderTest.getPaymentType(emptyMap());
    }

    @Test
    public void getDecisionReturnsValidValue() {
        assertThat(classUnderTest.getDecision(ImmutableMap.of(GENERAL_REFERRAL_DECISION, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDecisionShouldThrowInvalidDataForTaskException() {
        classUnderTest.getDecision(emptyMap());
    }

    @Test
    public void getDecisionReasonReturnsValidValue() {
        assertThat(classUnderTest.getDecisionReason(ImmutableMap.of(GENERAL_REFERRAL_DECISION_REASON, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDecisionReasonShouldThrowInvalidDataForTaskException() {
        classUnderTest.getDecisionReason(emptyMap());
    }

    @Test
    public void getApplicationAddedDateUnformattedReturnsValidValue() {
        assertThat(classUnderTest.getApplicationAddedDateUnformatted(ImmutableMap.of(GENERAL_APPLICATION_ADDED_DATE, DATE)), is(DATE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getApplicationAddedDateUnformattedShouldThrowInvalidDataForTaskException() {
        classUnderTest.getApplicationAddedDateUnformatted(emptyMap());
    }

    @Test
    public void getApplicationFromReturnsValidValue() {
        assertThat(classUnderTest.getApplicationFrom(ImmutableMap.of(GENERAL_APPLICATION_FROM, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getApplicationFromShouldThrowInvalidDataForTaskException() {
        classUnderTest.getApplicationFrom(emptyMap());
    }

    @Test
    public void getApplicationReferralDateUnformattedReturnsValidValue() {
        assertThat(classUnderTest.getApplicationReferralDateUnformatted(ImmutableMap.of(GENERAL_APPLICATION_REFERRAL_DATE, DATE)), is(DATE));
        assertThat(classUnderTest.getApplicationReferralDateUnformatted(emptyMap()), is(""));
    }

    @Test
    public void getAlternativeMediumReturnsValidValue() {
        assertThat(classUnderTest.getAlternativeMedium(ImmutableMap.of(ALTERNATIVE_SERVICE_MEDIUM, VALUE)), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getAlternativeMediumShouldThrowInvalidDataForTaskException() {
        classUnderTest.getAlternativeMedium(emptyMap());
    }

    @Test
    public void givenNoField_whenGetListOfGeneralReferrals_shouldReturnAnEmptyArray() {
        List<CollectionMember<DivorceGeneralReferral>> result = classUnderTest.getListOfGeneralReferrals(emptyMap());

        assertThat(result, Is.is(empty()));
    }

    @Test
    public void givenFieldWithAnEmptyArray_whenGetListOfGeneralReferrals_shouldReturnEmptyArray() {
        final List<CollectionMember<DivorceGeneralReferral>> myList = emptyList();

        List<CollectionMember<DivorceGeneralReferral>> result = classUnderTest.getListOfGeneralReferrals(ImmutableMap.of(CcdFields.GENERAL_REFERRALS, myList));

        assertThat(result, Is.is(empty()));
    }

    @Test
    public void givenFieldWithPopulatedArray_whenGetListOfGeneralReferrals_shouldReturnPopulatedArray() {
        final List<CollectionMember<DivorceGeneralReferral>> myList = asList(new CollectionMember<>());

        List<CollectionMember<DivorceGeneralReferral>> result = classUnderTest.getListOfGeneralReferrals(ImmutableMap.of(CcdFields.GENERAL_REFERRALS, myList));

        assertThat(result.size(), Is.is(1));
        assertThat(result, Is.is(myList));
    }
}