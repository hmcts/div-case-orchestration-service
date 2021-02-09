package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetSeparationFieldsTask;

import java.util.Map;

@Component
public class SeparationFieldsWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SetSeparationFieldsTask setSeparationFieldsTask;

    @Autowired
    public SeparationFieldsWorkflow(SetSeparationFieldsTask setSeparationFieldsTask) {
        this.setSeparationFieldsTask = setSeparationFieldsTask;
    }

    public Map<String, Object> run(Map<String, Object> payload) throws WorkflowException {
        return this.execute(new Task[] {
            setSeparationFieldsTask,
        }, payload);
    }
}
