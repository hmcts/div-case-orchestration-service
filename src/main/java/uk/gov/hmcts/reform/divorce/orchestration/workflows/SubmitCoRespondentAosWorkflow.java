package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToAosCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCoRespondentAosCase;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class SubmitCoRespondentAosWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final FormatDivorceSessionToAosCaseDataTask formatDivorceSessionToAosCaseDataTask;

    private final SubmitCoRespondentAosCase submitCoRespondentAosCaseTask;

    @Autowired
    public SubmitCoRespondentAosWorkflow(final FormatDivorceSessionToAosCaseDataTask formatDivorceSessionToAosCaseDataTask,
                                         final SubmitCoRespondentAosCase submitCoRespondentAosCaseTask) {
        this.formatDivorceSessionToAosCaseDataTask = formatDivorceSessionToAosCaseDataTask;
        this.submitCoRespondentAosCaseTask = submitCoRespondentAosCaseTask;
    }

    public Map<String, Object> run(final Map<String, Object> divorceSession, final String authToken) throws WorkflowException {
        return this.execute(
            new Task[] {
                formatDivorceSessionToAosCaseDataTask,
                submitCoRespondentAosCaseTask
            },
            divorceSession,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }
}
