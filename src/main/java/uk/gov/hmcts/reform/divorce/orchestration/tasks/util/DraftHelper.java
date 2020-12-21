package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_DRAFT_KEY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DraftHelper {

    public static boolean isDraft(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(IS_DRAFT_KEY))
            .map(Boolean.class::cast)
            .orElse(false);
    }
}
