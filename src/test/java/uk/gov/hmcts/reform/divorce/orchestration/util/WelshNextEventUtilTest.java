package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.BO_WELSH_DN_RECEIVED_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.BO_WELSH_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;

@RunWith(MockitoJUnitRunner.class)
public class WelshNextEventUtilTest {
    @InjectMocks
    private WelshNextEventUtil nextEventUtil;

    @Test
    public void testWelshStopEvent() {
        String nextEvent = "next_event";
        String welshEvent = "welsh_event";
        Map<String, Object> caseData = new HashMap<>();
        String resNextEvent = nextEventUtil.storeNextEventAndReturnStopEvent(() -> true, caseData, nextEvent, welshEvent, BO_WELSH_REVIEW);
        assertThat(resNextEvent).isEqualTo(BO_WELSH_REVIEW);
        assertThat(caseData).contains(entry(WELSH_NEXT_EVENT, welshEvent));
    }

    @Test
    public void testEnglishContinue() {
        String nextEvent = "next_event";
        String welshEvent = "welsh_event";
        Map<String, Object> caseData = new HashMap<>();
        String resNextEvent = nextEventUtil.storeNextEventAndReturnStopEvent(() -> false, caseData, nextEvent, welshEvent, BO_WELSH_REVIEW);
        assertThat(resNextEvent).isEqualTo(nextEvent);
        assertThat(caseData).doesNotContainKeys(WELSH_NEXT_EVENT);
    }

    @Test
    public void welshDNReceivedStopEvent() {
        String nextEvent = "next_event";
        String welshEvent = "welsh_event";
        Map<String, Object> caseData = new HashMap<>();
        String resNextEvent = nextEventUtil.storeNextEventAndReturnStopEvent(() -> true, caseData,
            nextEvent, welshEvent, BO_WELSH_DN_RECEIVED_REVIEW);
        assertThat(resNextEvent).isEqualTo(BO_WELSH_DN_RECEIVED_REVIEW);
        assertThat(caseData).contains(entry(WELSH_NEXT_EVENT, welshEvent));
    }

    @Test
    public void englishDNReceivedContinue() {
        String nextEvent = "next_event";
        String welshEvent = "welsh_event";
        Map<String, Object> caseData = new HashMap<>();
        String resNextEvent = nextEventUtil.storeNextEventAndReturnStopEvent(() -> false, caseData,
            nextEvent, welshEvent, BO_WELSH_DN_RECEIVED_REVIEW);
        assertThat(resNextEvent).isEqualTo(nextEvent);
        assertThat(caseData).doesNotContainKeys(WELSH_NEXT_EVENT);
    }
}
