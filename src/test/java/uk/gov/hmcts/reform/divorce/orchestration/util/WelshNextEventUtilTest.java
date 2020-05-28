package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;

@RunWith(MockitoJUnitRunner.class)
public class WelshNextEventUtilTest {
    @InjectMocks
    private WelshNextEventUtil nextEventUtil;

    @Test
    public void testWelshStopEvent() {
        String nextEvent = "next_event";
        Map<String, Object> caseData = new HashMap<>();
        String resNextEvent = nextEventUtil.evaluateEventId(() -> true, caseData, nextEvent);
        assertThat(resNextEvent).isEqualTo(BO_WELSH_REVIEW);
        assertThat(caseData).contains(entry(WELSH_NEXT_EVENT, nextEvent));
    }

    @Test
    public void testEnglishContinue() {
        String nextEvent = "next_event";
        Map<String, Object> caseData = new HashMap<>();
        String resNextEvent = nextEventUtil.evaluateEventId(() -> false, caseData, nextEvent);
        assertThat(resNextEvent).isEqualTo(nextEvent);
        assertThat(caseData).doesNotContainKeys(WELSH_NEXT_EVENT);
    }
}
