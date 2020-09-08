package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.CO_RESPONDENT_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_BOTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PartyRepresentationChecker {

    public static boolean isPetitionerRepresented(Map<String, Object> caseData) {
        String petitionerSolicitorEmail = (String) caseData.get(PETITIONER_SOLICITOR_EMAIL);

        return !Strings.isNullOrEmpty(petitionerSolicitorEmail);
    }

    public static boolean isRespondentRepresented(Map<String, Object> caseData) {
        return isRepresented(caseData, RESP_SOL_REPRESENTED);
    }

    public static boolean isCoRespondentLinkedToCase(Map<String, Object> caseData) {
        return isYesOnly(caseData, CO_RESPONDENT_LINKED_TO_CASE);
    }

    public static boolean isCoRespondentRepresented(Map<String, Object> caseData) {
        return isRepresented(caseData, CO_RESPONDENT_REPRESENTED);
    }

    public static boolean isRespondentDigital(Map<String, Object> caseData) {
        return isYesOrEmpty(caseData, RESP_IS_USING_DIGITAL_CHANNEL);
    }

    public static boolean isCoRespondentDigital(Map<String, Object> caseData) {
        return isYesOrEmpty(caseData, CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL);
    }

    public static boolean isCoRespondentLiableForCosts(Map<String, Object> caseData) {
        String whoPaysCosts = String.valueOf(caseData.get(WHO_PAYS_COSTS_CCD_FIELD));

        return WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT.equalsIgnoreCase(whoPaysCosts)
            || WHO_PAYS_CCD_CODE_FOR_BOTH.equalsIgnoreCase(whoPaysCosts);
    }

    private static boolean isRepresented(Map<String, Object> caseData, String field) {
        return isYesOnly(caseData, field);
    }

    private static boolean isYesOrEmpty(Map<String, Object> caseData, String field) {
        String value = (String) caseData.get(field);
        return Strings.isNullOrEmpty(value) || YES_VALUE.equalsIgnoreCase(value);
    }

    private static boolean isYesOnly(Map<String, Object> caseData, String field) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(field));
    }
}
