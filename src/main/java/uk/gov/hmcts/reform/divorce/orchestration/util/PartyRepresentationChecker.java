package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PartyRepresentationChecker {

    public static boolean isPetitionerRepresented(Map<String, Object> caseData) {
        String petitionerSolicitorEmail = (String) caseData.get(PET_SOL_EMAIL);

        return !Strings.isNullOrEmpty(petitionerSolicitorEmail);
    }

    public static boolean isRespondentRepresented(Map<String, Object> caseData) {
        return isRepresented(caseData, RESP_SOL_REPRESENTED);
    }

    public static boolean isCoRespondentRepresented(Map<String, Object> caseData) {
        return isRepresented(caseData, CO_RESPONDENT_REPRESENTED);
    }

    private static boolean isRepresented(Map<String, Object> caseData, String field) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(field));
    }
}
