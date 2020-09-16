package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceApplicationRefusalHelper {
    public static String getServiceApplicationRefusalReason(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, SERVICE_APPLICATION_REFUSAL_REASON);
    }

    public static String getServiceApplicationGranted(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, SERVICE_APPLICATION_GRANTED);
    }

    public static String getServiceApplicationType(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, SERVICE_APPLICATION_TYPE);
    }

    public static boolean isAwaitingServiceConsideration(CaseDetails caseDetails) {
        return AWAITING_SERVICE_CONSIDERATION.equalsIgnoreCase(caseDetails.getState());
    }

    public static boolean isServiceApplicationGranted(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(getMandatoryStringValue(caseData, SERVICE_APPLICATION_GRANTED));
    }
}
