package uk.gov.hmcts.reform.divorce.orchestration.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EventConfigTest {

    @Autowired
    private EventConfig eventConfig;
    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void testEnglishEvent() {
        String expected = "aosSubmittedDefended";
        String actual = eventConfig.getEvents().get(LanguagePreference.ENGLISH).get(EventType.getEvenType(expected));
        assertEquals("English event", actual, expected);
    }

    @Test
    public void testWelshEvent() {
        String input = "aosSubmittedDefended";
        String expected = "aosSubmittedDefendedWelshReview";
        String actual = eventConfig.getEvents().get(LanguagePreference.WELSH).get(EventType.getEvenType(input));
        assertEquals("English event", actual, expected);
    }

    @Test
    public void testIllegalArgumentException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Invalid event id :aosSubmittedDefendedWelshReview"));

        String input = "aosSubmittedDefendedWelshReview";
        eventConfig.getEvents().get(LanguagePreference.WELSH).get(EventType.getEvenType(input));
     }
}

