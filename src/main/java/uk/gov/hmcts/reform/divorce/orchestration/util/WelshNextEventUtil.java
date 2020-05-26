package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;

@Component
public class WelshNextEventUtil {

    public String evaluateEventId(Supplier<Boolean> isWelsh, Map<String, Object> caseData,  String eventId) {
        return Optional.of(isWelsh.get())
            .filter(Predicate.isEqual(true))
            .map(k -> {
                caseData.put(WELSH_NEXT_EVENT, eventId);
                return BO_WELSH_REVIEW; })
            .orElse(eventId);
    }
}
