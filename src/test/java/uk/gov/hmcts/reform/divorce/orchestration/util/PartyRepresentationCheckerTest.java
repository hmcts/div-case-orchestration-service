package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

public class PartyRepresentationCheckerTest {

    @Test
    public void isPetitionerRepresentedReturnsTrue() {
        assertThat(isPetitionerRepresented(createCaseData(PET_SOL_EMAIL, "I-represent@petitioner.com")), is(true));
    }

    @Test
    public void isPetitionerRepresentedReturnsFalse() {
        assertThat(isPetitionerRepresented(createCaseData(PET_SOL_EMAIL, "")), is(false));
        assertThat(isPetitionerRepresented(createCaseData(PET_SOL_EMAIL, null)), is(false));
        assertThat(isPetitionerRepresented(createCaseData("another-field", "value")), is(false));
        assertThat(isPetitionerRepresented(EMPTY_MAP), is(false));
    }

    @Test
    public void isRespondentRepresentedReturnsTrue() {
        assertThat(isRespondentRepresented(createCaseData(RESP_SOL_REPRESENTED, YES_VALUE)), is(true));
    }

    @Test
    public void isRespondentRepresentedReturnsFalse() {
        assertThat(isRespondentRepresented(createCaseData(RESP_SOL_REPRESENTED, NO_VALUE)), is(false));
        assertThat(isRespondentRepresented(createCaseData(RESP_SOL_REPRESENTED, null)), is(false));
        assertThat(isRespondentRepresented(createCaseData("another-field-1", YES_VALUE)), is(false));
        assertThat(isRespondentRepresented(createCaseData("another-field-2", NO_VALUE)), is(false));
        assertThat(isRespondentRepresented(EMPTY_MAP), is(false));
    }

    @Test
    public void isCoRespondentRepresentedReturnsTrue() {
        assertThat(isCoRespondentRepresented(createCaseData(CO_RESPONDENT_REPRESENTED, YES_VALUE)), is(true));
    }

    @Test
    public void isCoRespondentRepresentedReturnsFalse() {
        assertThat(isCoRespondentRepresented(createCaseData(CO_RESPONDENT_REPRESENTED, NO_VALUE)), is(false));
        assertThat(isCoRespondentRepresented(createCaseData(CO_RESPONDENT_REPRESENTED, null)), is(false));
        assertThat(isCoRespondentRepresented(createCaseData("another-field-1", YES_VALUE)), is(false));
        assertThat(isCoRespondentRepresented(createCaseData("another-field-2", NO_VALUE)), is(false));
        assertThat(isCoRespondentRepresented(EMPTY_MAP), is(false));
    }

    private static Map<String, Object> createCaseData(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }
}
