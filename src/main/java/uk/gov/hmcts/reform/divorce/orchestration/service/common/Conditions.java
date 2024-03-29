package uk.gov.hmcts.reform.divorce.orchestration.service.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Conditions {

    public static boolean isServiceApplicationGranted(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(CcdFields.SERVICE_APPLICATION_GRANTED));
    }

    public static boolean isServiceApplicationGranted(DivorceServiceApplication serviceApplication) {
        return YES_VALUE.equalsIgnoreCase(serviceApplication.getApplicationGranted());
    }

    public static boolean isServiceApplicationDispensed(Map<String, Object> caseData) {
        return ApplicationServiceTypes.DISPENSED.equalsIgnoreCase((String) caseData.get(CcdFields.SERVICE_APPLICATION_TYPE));
    }

    public static boolean isServiceApplicationDispensed(DivorceServiceApplication serviceApplication) {
        return ApplicationServiceTypes.DISPENSED.equalsIgnoreCase(serviceApplication.getType());
    }

    public static boolean isServiceApplicationDeemed(Map<String, Object> caseData) {
        return ApplicationServiceTypes.DEEMED.equalsIgnoreCase((String) caseData.get(CcdFields.SERVICE_APPLICATION_TYPE));
    }

    public static boolean isServiceApplicationDeemed(DivorceServiceApplication serviceApplication) {
        return ApplicationServiceTypes.DEEMED.equalsIgnoreCase(serviceApplication.getType());
    }

    public static boolean isServiceApplicationDeemedDispensedOrBailiff(DivorceServiceApplication serviceApplication) {
        return isServiceApplicationDeemedOrDispensed(serviceApplication) || isServiceApplicationBailiff(serviceApplication);
    }

    public static boolean isServiceApplicationDeemedOrDispensed(DivorceServiceApplication serviceApplication) {
        return isServiceApplicationDeemed(serviceApplication) || isServiceApplicationDispensed(serviceApplication);
    }

    public static boolean isServiceApplicationBailiff(DivorceServiceApplication serviceApplication) {
        return ApplicationServiceTypes.BAILIFF.equalsIgnoreCase(serviceApplication.getType());
    }

    public static boolean isServiceApplicationBailiff(Map<String, Object> caseData) {
        return ApplicationServiceTypes.BAILIFF.equalsIgnoreCase((String) caseData.get(CcdFields.SERVICE_APPLICATION_TYPE));
    }

    public static boolean isBailiffServiceSuccessful(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(CcdFields.BAILIFF_SERVICE_SUCCESSFUL));
    }

    public static boolean isAwaitingServiceConsideration(CaseDetails caseDetails) {
        return CcdStates.AWAITING_SERVICE_CONSIDERATION.equalsIgnoreCase(caseDetails.getState());
    }

    public static boolean isAOSDraftedCandidate(CaseDetails caseDetails) {
        return caseDetails.getState().equals(AOS_AWAITING_SOLICITOR)
            || caseDetails.getState().equals(AOS_AWAITING)
            || caseDetails.getState().equals(AOS_OVERDUE);
    }

    public static boolean isPetitionAmended(Map<String, Object> caseData) {
        Map<String, Object> previousCaseId = (Map<String, Object>) caseData.get(PREVIOUS_CASE_ID_CCD_KEY);

        return previousCaseId != null;
    }
}
