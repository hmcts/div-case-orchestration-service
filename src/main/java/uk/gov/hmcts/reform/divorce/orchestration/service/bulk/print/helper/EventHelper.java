package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Stream;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.ISSUE_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.ISSUE_AOS_FROM_REISSUE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventHelper {

    public static boolean isIssueAosEvent(String eventId) {
        return Stream.of(ISSUE_AOS, ISSUE_AOS_FROM_REISSUE)
            .anyMatch(supportEvent -> supportEvent.equalsIgnoreCase(eventId));
    }
}
