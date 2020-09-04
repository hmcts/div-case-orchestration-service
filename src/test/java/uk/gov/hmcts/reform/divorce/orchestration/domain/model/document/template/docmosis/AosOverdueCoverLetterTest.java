package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AosOverdueCoverLetterTest {

    @Test
    public void shouldAssembleTemplateVars_WithHelpWithFeesNumber() {
        AosOverdueCoverLetter templateVars = AosOverdueCoverLetter.aosOverdueCoverLetterBuilder().helpWithFeesNumber("123").build();

        assertThat(templateVars.getHelpWithFeesNumber(), is("123"));
        assertThat(templateVars.isHasHelpWithFeesNumber(), is(true));
    }

    @Test
    public void shouldAssembleTemplateVars_WithBlankHelpWithFeesNumber() {
        AosOverdueCoverLetter templateVars = AosOverdueCoverLetter.aosOverdueCoverLetterBuilder().helpWithFeesNumber("").build();

        assertThat(templateVars.getHelpWithFeesNumber(), is(nullValue()));
        assertThat(templateVars.isHasHelpWithFeesNumber(), is(false));
    }

    @Test
    public void shouldAssembleTemplateVars_WithNoHelpWithFeesNumber() {
        AosOverdueCoverLetter templateVars = AosOverdueCoverLetter.aosOverdueCoverLetterBuilder().build();

        assertThat(templateVars.getHelpWithFeesNumber(), is(nullValue()));
        assertThat(templateVars.isHasHelpWithFeesNumber(), is(false));
    }

}