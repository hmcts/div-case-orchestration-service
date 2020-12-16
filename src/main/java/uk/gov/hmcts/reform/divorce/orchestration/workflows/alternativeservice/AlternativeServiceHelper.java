package uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_PROCESS_SERVER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AlternativeServiceHelper {

    public static boolean isServedByAlternativeMethod(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(CcdFields.SERVED_BY_ALTERNATIVE_METHOD));
    }

    public static boolean isServedByProcessServer(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(SERVED_BY_PROCESS_SERVER));
    }
}
