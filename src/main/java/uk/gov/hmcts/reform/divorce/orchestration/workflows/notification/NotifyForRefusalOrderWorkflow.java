package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.NotifyForRefusalOrderTask;

import java.util.Map;

@Component
public class NotifyForRefusalOrderWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private NotifyForRefusalOrderTask notifyForRefusalOrderTask;

    public Map<String, Object> run(Map<String, Object> caseData) throws WorkflowException {
        return this.execute(new Task[] {
                notifyForRefusalOrderTask
            },
            caseData
        );
    }
}
