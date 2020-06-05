package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

public class PartyRepresentationCheckerTest {

    @Test
    public void isRespondentRepresentedReturnsTrue() {
        assertThat(isRespondentRepresented(ImmutableMap.of(RESP_SOL_REPRESENTED, YES_VALUE)), is(true));
    }

    @Test
    public void isRespondentRepresentedReturnsFalse() {
        assertThat(isRespondentRepresented(ImmutableMap.of(RESP_SOL_REPRESENTED, NO_VALUE)), is(false));
        assertThat(isRespondentRepresented(ImmutableMap.of("another-field-1", YES_VALUE)), is(false));
        assertThat(isRespondentRepresented(ImmutableMap.of("another-field-2", NO_VALUE)), is(false));
        assertThat(isRespondentRepresented(EMPTY_MAP), is(false));
    }

    @Test
    public void isCoRespondentRepresentedReturnsTrue() {
        assertThat(isCoRespondentRepresented(ImmutableMap.of(CO_RESPONDENT_REPRESENTED, YES_VALUE)), is(true));
    }

    @Test
    public void isCoRespondentRepresentedReturnsFalse() {
        assertThat(isCoRespondentRepresented(ImmutableMap.of(CO_RESPONDENT_REPRESENTED, NO_VALUE)), is(false));
        assertThat(isCoRespondentRepresented(ImmutableMap.of("another-field-1", YES_VALUE)), is(false));
        assertThat(isCoRespondentRepresented(ImmutableMap.of("another-field-2", NO_VALUE)), is(false));
        assertThat(isCoRespondentRepresented(EMPTY_MAP), is(false));
    }
}
