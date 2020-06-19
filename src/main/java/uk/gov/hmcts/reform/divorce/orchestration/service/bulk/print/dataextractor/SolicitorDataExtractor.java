package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;

public class SolicitorDataExtractor {

    public static String getSolicitorReference(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CoELetterDataExtractor.CaseDataKeys.SOLICITOR_REFERENCE);
    }

}
