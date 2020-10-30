package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getAsDynamicList;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SolicitorDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String SOLICITOR_REFERENCE = OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
        public static final String SOLICITOR_PAYMENT_METHOD = OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
        public static final String SOLICITOR_PBA_NUMBER_V1 = OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;
        public static final String SOLICITOR_PBA_NUMBER_V2 = CcdFields.PBA_NUMBERS;
    }

    public static String getSolicitorReference(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.SOLICITOR_REFERENCE, "");
    }

    public static String getPaymentMethod(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.SOLICITOR_PAYMENT_METHOD, "");
    }

    public static String getPbaNumber(Map<String, Object> caseData, boolean isToggleOn) {
        if (isToggleOn) {
            DynamicList pbaNumbers = getAsDynamicList(caseData, CaseDataKeys.SOLICITOR_PBA_NUMBER_V2);
            return pbaNumbers.getValue().getCode();
        }
        return getMandatoryPropertyValueAsString(caseData, CaseDataKeys.SOLICITOR_PBA_NUMBER_V1);
    }
}
