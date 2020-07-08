package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AOS_AWAITING_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AOS_DRAFTED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AOS_PRE_SUBMITTED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AWAITING_HWF_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AWAITING_REISSUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.AWAITING_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.INVALID_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.ISSUED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.PENDING_REJECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.REJECTED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.SOLICITOR_AWAITING_PAYMENT_CONFIRMATION;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.PreviousAmendPetitionStateLoggerHelper.SUBMITTED;

public class PreviousAmendPetitionStateLoggerHelperTest {

    @Test
    public void shouldReturnInValidState_WhenStateIsNull() {
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(null), is(INVALID_STATE));
    }

    @Test
    public void shouldReturnInValidState_WhenStateIsEmptyString() {
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(""), is(INVALID_STATE));
    }

    @Test
    public void shouldReturnInValidState_WhenStateIsNotAppropriateState() {
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState("HavingAGreatTime"), is(INVALID_STATE));
    }

    @Test
    public void shouldReturnValidState_ForAmendPetition() {
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AOS_AWAITING), is(AOS_AWAITING));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AOS_AWAITING_SOLICITOR), is(AOS_AWAITING_SOLICITOR));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AOS_COMPLETED), is(AOS_COMPLETED));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AOS_DRAFTED), is(AOS_DRAFTED));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AOS_OVERDUE), is(AOS_OVERDUE));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AOS_PRE_SUBMITTED), is(AOS_PRE_SUBMITTED));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AOS_STARTED), is(AOS_STARTED));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AOS_SUBMITTED_AWAITING_ANSWER),
            is(AOS_SUBMITTED_AWAITING_ANSWER));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AWAITING_DECREE_NISI), is(AWAITING_DECREE_NISI));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AWAITING_HWF_DECISION), is(AWAITING_HWF_DECISION));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AWAITING_REISSUE), is(AWAITING_REISSUE));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(AWAITING_SERVICE), is(AWAITING_SERVICE));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(ISSUED), is(ISSUED));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(PENDING_REJECTION), is(PENDING_REJECTION));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(REJECTED), is(REJECTED));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(SOLICITOR_AWAITING_PAYMENT_CONFIRMATION),
            is(SOLICITOR_AWAITING_PAYMENT_CONFIRMATION));
        assertThat(PreviousAmendPetitionStateLoggerHelper.getAmendPetitionPreviousState(SUBMITTED), is(SUBMITTED));
    }
}