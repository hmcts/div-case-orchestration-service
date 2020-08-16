package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceRefusalDecision;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceRefusalDecision.DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceRefusalDecision.FINAL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceApplicationRefusalHelper {
    public static String getServiceApplicationRefusalReason(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CcdFields.SERVICE_APPLICATION_REFUSAL_REASON);
    }

    public static String getServiceApplicationGranted(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, SERVICE_APPLICATION_GRANTED);
    }

    public static String getServiceApplicationType(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CcdFields.SERVICE_APPLICATION_TYPE);
    }

    public static String getCaseReference(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CASE_ID_JSON_KEY);
    }

    public static boolean isFinal(ServiceRefusalDecision serviceDecision) {
        return FINAL.equals(serviceDecision);
    }

    public static boolean isDraft(ServiceRefusalDecision serviceDecision) {
        return DRAFT.equals(serviceDecision);
    }

    public static boolean isDispensedApplication(String applicationType) {
        return ApplicationServiceTypes.DISPENSED.equals(applicationType);
    }

    public static boolean isDeemedApplication(String applicationType) {
        return ApplicationServiceTypes.DEEMED.equals(applicationType);
    }

    public static boolean isAwaitingServiceConsideration(CaseDetails caseDetails) {
        return AWAITING_SERVICE_CONSIDERATION.equals(caseDetails.getState());
    }

    public static boolean isServiceApplicationGranted(Map<String, Object> caseData) {
        return YES_VALUE.equals(getMandatoryStringValue(caseData, SERVICE_APPLICATION_GRANTED));
    }
}
