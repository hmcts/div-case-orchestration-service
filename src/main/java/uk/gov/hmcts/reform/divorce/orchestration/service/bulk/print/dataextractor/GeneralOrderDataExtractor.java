package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.GeneralOrderParty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryListOfStrings;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneralOrderDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String GENERAL_ORDER_DATE = CcdFields.GENERAL_ORDER_DATE;
        public static final String JUDGE_TYPE = CcdFields.JUDGE_TYPE;
        public static final String JUDGE_NAME = CcdFields.JUDGE_NAME;
        public static final String GENERAL_ORDER_DETAILS = CcdFields.GENERAL_ORDER_DETAILS;
        public static final String GENERAL_ORDER_RECITALS = CcdFields.GENERAL_ORDER_RECITALS;
        public static final String GENERAL_ORDER_PARTIES = CcdFields.GENERAL_ORDER_PARTIES;
    }

    public static String getJudgeName(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.JUDGE_NAME);
    }

    public static String getJudgeType(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.JUDGE_TYPE);
    }

    public static String getGeneralOrderRecitals(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.GENERAL_ORDER_RECITALS, "").trim();
    }

    public static String getGeneralOrderDetails(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_ORDER_DETAILS);
    }

    public static String getGeneralOrderDate(Map<String, Object> caseData) {
        return formatDateWithCustomerFacingFormat(getMandatoryStringValue(caseData, CaseDataKeys.GENERAL_ORDER_DATE));
    }

    public static List<GeneralOrderParty> getGeneralOrderParties(Map<String, Object> caseData) {
        return getMandatoryListOfStrings(caseData, CaseDataKeys.GENERAL_ORDER_PARTIES).stream()
            .map(GeneralOrderParty::from)
            .collect(Collectors.toList());
    }
}
