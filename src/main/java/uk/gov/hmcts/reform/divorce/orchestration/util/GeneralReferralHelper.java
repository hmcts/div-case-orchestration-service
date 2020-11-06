package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.isYes;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneralReferralHelper {

    public static boolean isGeneralReferralPaymentRequired(Map<String, Object> caseData)  {
        return isYes(GeneralReferralDataExtractor.getIsFeeRequired(caseData));
    }

    public static boolean isReasonGeneralApplicationReferral(Map<String, Object> caseData) {
        return GeneralReferralDataExtractor.getReason(caseData).equals(CcdFields.GENERAL_APPLICATION_REFERRAL);
    }

    public static boolean isTypeOfAlternativeServiceApplication(Map<String, Object> caseData) {
        return GeneralReferralDataExtractor.getType(caseData).equals(CcdFields.ALTERNATIVE_SERVICE_APPLICATION);
    }

    public static boolean isGeneralReferralRejected(Map<String, Object> caseData) {
        return GeneralReferralDataExtractor.getDecision(caseData).equals(CcdFields.GENERAL_REFERRAL_DECISION_REFUSE);
    }

}
