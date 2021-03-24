package uk.gov.hmcts.reform.divorce.orchestration.workflows.aos;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aos.AosOfflineTriggerRequestTask;

@Component
public class AosOfflineTriggerRequestWorkflow extends DefaultWorkflow<String> {

    private final Task[] tasks;

    public AosOfflineTriggerRequestWorkflow(AosOfflineTriggerRequestTask aosOfflineTriggerRequestTask) {
        this.tasks = new Task[] {aosOfflineTriggerRequestTask};
    }

    public void requestAosOfflineToBeTriggered(String caseId) throws WorkflowException {
        execute(tasks, caseId);
    }

}