package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CORESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PartyRepresentationChecker {

    public static boolean isRespondentRepresented(Map<String, Object> caseData) {
        return isRepresented(caseData, RESP_SOL_REPRESENTED);
    }

    public static boolean isCoRespondentRepresented(Map<String, Object> caseData) {
        return isRepresented(caseData, CORESPONDENT_REPRESENTED);
    }

    private static boolean isRepresented(Map<String, Object> caseData, String corespondentRepresented) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(corespondentRepresented));
    }
}
