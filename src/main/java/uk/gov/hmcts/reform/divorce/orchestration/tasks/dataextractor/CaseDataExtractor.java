package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseDataExtractor extends Extractable {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String CASE_REFERENCE = OrchestrationConstants.D_8_CASE_REFERENCE;
    }

    public static String getCaseReference(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.CASE_REFERENCE);
    }
}
