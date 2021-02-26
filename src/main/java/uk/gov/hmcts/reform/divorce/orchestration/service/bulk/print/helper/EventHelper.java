package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Stream;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.ISSUE_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.ISSUE_AOS_FROM_REISSUE_EVENT_ID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventHelper {

    public static boolean isIssueAosEvent(String eventId) {
        return Stream.of(ISSUE_AOS_EVENT_ID, ISSUE_AOS_FROM_REISSUE_EVENT_ID)
            .anyMatch(supportEvent -> supportEvent.equalsIgnoreCase(eventId));
    }
}
