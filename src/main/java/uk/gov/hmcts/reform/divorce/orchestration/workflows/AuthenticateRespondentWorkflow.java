package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AuthenticateRespondent;

@Component
public class AuthenticateRespondentWorkflow extends DefaultWorkflow<Boolean> {
    private final AuthenticateRespondent authenticateRespondent;

    @Autowired
    public AuthenticateRespondentWorkflow(AuthenticateRespondent authenticateRespondent) {
        this.authenticateRespondent = authenticateRespondent;
    }

    public Boolean run(String authToken) throws WorkflowException {
        return this.execute(new Task[] {
            authenticateRespondent,
        }, null, authToken);
    }
}
