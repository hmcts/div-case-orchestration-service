package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraftTask;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class DeleteDraftWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final DeleteDraftTask deleteDraftTask;

    @Autowired
    public DeleteDraftWorkflow(DeleteDraftTask deleteDraftTask) {
        this.deleteDraftTask = deleteDraftTask;
    }

    public Map<String, Object> run(String authToken) throws WorkflowException {
        return this.execute(
            new Task[]{
                deleteDraftTask
            },
            new HashMap<>(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }
}
