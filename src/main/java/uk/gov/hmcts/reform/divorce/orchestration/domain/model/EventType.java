package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum EventType {
    aosSubmittedDefended("aosSubmittedDefended"),
    aosReceivedNoAdConStarted("aosReceivedNoAdConStarted"),
    aosSubmittedUndefended("aosSubmittedUndefended");

    private final String eventId;

    EventType(String eventId) {
        this.eventId = eventId;
    }

    public static EventType getEvenType(String eventId) {
        return Arrays.stream(EventType.values()).filter(eventType -> eventType.getEventId().equals(eventId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.join("","Invalid event id :", eventId)));
    }
}
