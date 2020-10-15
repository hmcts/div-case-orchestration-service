package uk.gov.hmcts.reform.divorce.orchestration.service.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Conditions {

    public static boolean isServiceApplicationGranted(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(CcdFields.SERVICE_APPLICATION_GRANTED));
    }

    public static boolean isServiceApplicationDispensed(Map<String, Object> caseData) {
        return ApplicationServiceTypes.DISPENSED.equalsIgnoreCase((String) caseData.get(CcdFields.SERVICE_APPLICATION_TYPE));
    }

    public static boolean isServiceApplicationDeemed(Map<String, Object> caseData) {
        return ApplicationServiceTypes.DEEMED.equalsIgnoreCase((String) caseData.get(CcdFields.SERVICE_APPLICATION_TYPE));
    }

    public static boolean isAwaitingServiceConsideration(CaseDetails caseDetails) {
        return CcdStates.AWAITING_SERVICE_CONSIDERATION.equalsIgnoreCase(caseDetails.getState());
    }

    public static boolean isGeneralReferralPaymentRequired(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(CcdFields.GENERAL_REFERRAL_FEE));
    }
}
