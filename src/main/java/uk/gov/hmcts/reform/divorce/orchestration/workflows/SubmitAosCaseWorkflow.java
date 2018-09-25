package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToAosCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitAosCase;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class SubmitAosCaseWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private FormatDivorceSessionToAosCaseData formatDivorceSessionToAosCaseData;

    @Autowired
    private SubmitAosCase submitAosCase;

    public Map<String, Object> run(Map<String, Object> payload,
                                   String authToken,
                                   String caseId) throws WorkflowException {
        return this.execute(
            new Task[] {
                formatDivorceSessionToAosCaseData,
                submitAosCase
            },
            payload,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
