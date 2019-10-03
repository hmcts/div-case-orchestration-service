package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetAmendPetitionFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.NotifyForRefusalOrderTask;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotifyForRefusalOrderWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final NotifyForRefusalOrderTask notifyForRefusalOrderTask;
    private final GetAmendPetitionFeeTask getAmendPetitionFeeTask;

    public Map<String, Object> run(Map<String, Object> caseData) throws WorkflowException {
        return this.execute(
            new Task[] { getAmendPetitionFeeTask, notifyForRefusalOrderTask },
            caseData
        );
    }
}
