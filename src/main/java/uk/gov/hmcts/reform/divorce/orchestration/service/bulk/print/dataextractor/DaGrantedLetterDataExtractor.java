package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DaGrantedLetterDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String DA_GRANTED_DATE = OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
    }

    public static String getDaGrantedDate(Map<String, Object> caseData) {
        return formatDateWithCustomerFacingFormat(getMandatoryStringValue(caseData, CaseDataKeys.DA_GRANTED_DATE));
    }
}
