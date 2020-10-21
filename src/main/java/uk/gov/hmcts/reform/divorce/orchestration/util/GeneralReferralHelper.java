package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneralReferralHelper {

    public static boolean isGeneralReferralPaymentRequired(Map<String, Object> caseData)  {
        return YES_VALUE.equalsIgnoreCase(getMandatoryStringValue(caseData, CcdFields.GENERAL_REFERRAL_FEE));
    }

}
