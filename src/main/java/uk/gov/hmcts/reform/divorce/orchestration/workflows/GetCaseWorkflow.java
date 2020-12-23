package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddCourtsToPayloadTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrdersFilterTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CASE_ID_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CASE_STATE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.COURT_KEY;

@Component
@RequiredArgsConstructor
public class GetCaseWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GetCaseTask getCaseTask;
    private final GeneralOrdersFilterTask generalOrdersFilterTask;
    private final CaseDataToDivorceFormatterTask caseDataToDivorceFormatterTask;
    private final AddCourtsToPayloadTask addCourtsToPayloadTask;

    public Map<String, Object> run(String authToken) throws WorkflowException {
        return execute(
            new Task[] {
                getCaseTask,
                generalOrdersFilterTask,
                caseDataToDivorceFormatterTask,
                addCourtsToPayloadTask
            },
            null,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }

    public String getCaseId() {
        return getContext().getTransientObject(CASE_ID_KEY);
    }

    public String getCaseState() {
        return getContext().getTransientObject(CASE_STATE_KEY);
    }

    public String getCourt() {
        return getContext().getTransientObject(COURT_KEY);
    }

}