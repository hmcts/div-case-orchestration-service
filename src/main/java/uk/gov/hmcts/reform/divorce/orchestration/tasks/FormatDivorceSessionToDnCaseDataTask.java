package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FORMATTER_CASE_DATA_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FORMATTER_DIVORCE_SESSION_KEY;

@Component
@RequiredArgsConstructor
public class FormatDivorceSessionToDnCaseDataTask implements Task<Map<String, Object>> {

    private final CaseFormatterClient caseFormatterClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        if (AWAITING_CLARIFICATION.equalsIgnoreCase(caseDetails.getState())) {
            Map<String, Object> divorceCaseWrapper = ImmutableMap.of(
                FORMATTER_CASE_DATA_KEY, caseDetails.getCaseData(),
                FORMATTER_DIVORCE_SESSION_KEY, sessionData
            );
            return caseFormatterClient.transformToDnClarificationCaseFormat(divorceCaseWrapper);
        }

        return caseFormatterClient.transformToDnCaseFormat(sessionData);
    }
}
