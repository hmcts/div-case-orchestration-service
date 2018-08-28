package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveDraft;

import java.util.HashMap;
import java.util.Map;

@Component
public class RetrieveDraftWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final RetrieveDraft retrieveDraft;

    @Autowired
    public RetrieveDraftWorkflow(RetrieveDraft retrieveDraft) {
        this.retrieveDraft = retrieveDraft;
    }

    public Map<String, Object> run(String authToken) throws WorkflowException {
        return this.execute(
                new Task[]{
                    retrieveDraft
                },
                new HashMap<String, Object>(),
                authToken);
    }

}
