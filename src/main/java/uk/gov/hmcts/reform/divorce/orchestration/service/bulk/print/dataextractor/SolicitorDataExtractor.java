package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.FeatureToggleServiceImpl;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsObject;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SolicitorDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String SOLICITOR_REFERENCE = OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
        public static final String SOLICITOR_PAYMENT_METHOD = OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
        public static final String SOLICITOR_PBA_NUMBER_V1 = OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;
        public static final String SOLICITOR_PBA_NUMBER_V2 = CcdFields.PBA_NUMBERS;
        private static FeatureToggleServiceImpl featureToggleService = new FeatureToggleServiceImpl();
        private static final ObjectMapper objectMapper = new ObjectMapper();
    }

    public static String getSolicitorReference(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.SOLICITOR_REFERENCE, "");
    }

    public static String getPaymentMethod(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.SOLICITOR_PAYMENT_METHOD, "");
    }

    public static String getPbaNumber(Map<String, Object> caseData) {
        final FeatureToggleServiceImpl featureToggleService = CaseDataKeys.featureToggleService;
        if (featureToggleService.isFeatureEnabled(Features.PAY_BY_ACCOUNT)) {
            log.info("PBA feature toggle on. Return new PBA field.");
            DynamicList pbaNumbers = CaseDataKeys.objectMapper.convertValue(
                getMandatoryPropertyValueAsObject(caseData, CaseDataKeys.SOLICITOR_PBA_NUMBER_V2), DynamicList.class);
            return pbaNumbers.getValue().getCode();
        }
        log.info("PBA feature toggle off. Return old PBA field.");
        return getMandatoryPropertyValueAsString(caseData, CaseDataKeys.SOLICITOR_PBA_NUMBER_V1);
    }

    public static void setFeatureToggleService(FeatureToggleServiceImpl featureToggleService) {
        CaseDataKeys.featureToggleService = featureToggleService;
    }
}
