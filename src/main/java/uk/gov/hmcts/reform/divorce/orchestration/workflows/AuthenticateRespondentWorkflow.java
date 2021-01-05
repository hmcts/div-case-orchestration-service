package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AuthenticateRespondentTask;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class AuthenticateRespondentWorkflow extends DefaultWorkflow<Boolean> {
    private final AuthenticateRespondentTask authenticateRespondentTask;

    @Autowired
    public AuthenticateRespondentWorkflow(AuthenticateRespondentTask authenticateRespondentTask) {
        this.authenticateRespondentTask = authenticateRespondentTask;
    }

    public Boolean run(String authToken) throws WorkflowException {
        return this.execute(
            new Task[] {
                authenticateRespondentTask,
            },
            null,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }
}
