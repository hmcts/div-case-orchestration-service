package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FORMATTER_CASE_DATA_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FORMATTER_DIVORCE_SESSION_KEY;

@Component
public class FormatDivorceSessionToDnCaseData implements Task<Map<String, Object>> {

    private final CaseFormatterClient caseFormatterClient;

    @Autowired
    public FormatDivorceSessionToDnCaseData(CaseFormatterClient caseFormatterClient) {
        this.caseFormatterClient = caseFormatterClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        if (AWAITING_CLARIFICATION.equalsIgnoreCase(caseDetails.getState())) {
            Map<String, Object> divorceCaseWrapper = ImmutableMap.of(
                FORMATTER_CASE_DATA_KEY, caseDetails.getCaseData(),
                FORMATTER_DIVORCE_SESSION_KEY, sessionData
            );
            return caseFormatterClient.transformToDnClarificationCaseFormat(divorceCaseWrapper);
        } else {
            return caseFormatterClient.transformToDnCaseFormat(
                sessionData
            );
        }
    }
}
