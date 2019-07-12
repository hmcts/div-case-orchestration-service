package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetSeparationFields;

import java.util.Map;

@Component
public class SeparationFieldsWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SetSeparationFields setSeparationFields;

    @Autowired
    public SeparationFieldsWorkflow(SetSeparationFields setSeparationFields) {
        this.setSeparationFields = setSeparationFields;
    }

    public Map<String, Object> run(Map<String, Object> payload) throws WorkflowException {

        return this.execute(new Task[] {
            setSeparationFields,
        }, payload);
    }
}
