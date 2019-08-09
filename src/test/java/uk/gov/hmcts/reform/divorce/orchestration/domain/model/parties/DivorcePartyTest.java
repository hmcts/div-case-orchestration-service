package uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

public class DivorcePartyTest {

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldGetRightEnumByString() throws DivorcePartyNotFoundException {
        assertThat(DivorceParty.getDivorcePartyByDescription("respondent"), equalTo(DivorceParty.RESPONDENT));
        assertThat(DivorceParty.getDivorcePartyByDescription("co-respondent"), equalTo(DivorceParty.CO_RESPONDENT));
    }

    @Test
    public void shouldThrowExceptionWhenDescriptionIsInvalid() throws DivorcePartyNotFoundException {
        expectedException.expect(DivorcePartyNotFoundException.class);
        expectedException.expectMessage("Could not find divorce party with the given description: " + "invalid description");

        DivorceParty.getDivorcePartyByDescription("invalid description");
    }

}