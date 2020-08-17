package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

public class ServiceRefusalDecisionTest {

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldGetRightEnumByString() throws ServiceRefusalDecisionNotFoundException {
        assertThat(ServiceRefusalDecision.getDecisionByName("final"), equalTo(ServiceRefusalDecision.FINAL));
        assertThat(ServiceRefusalDecision.getDecisionByName("draft"), equalTo(ServiceRefusalDecision.DRAFT));
    }

    @Test
    public void shouldThrowExceptionWhenDescriptionIsInvalid() throws ServiceRefusalDecisionNotFoundException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("invalid refusal decision value");

        ServiceRefusalDecision.getDecisionByName("invalid refusal decision value");
    }
}