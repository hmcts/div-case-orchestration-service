package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetCourtDetails;

import java.util.Map;

@Component
public class SolicitorCreateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SetCourtDetails setCourtDetails;

    @Autowired
    public SolicitorCreateWorkflow(SetCourtDetails setCourtDetails) {
        this.setCourtDetails = setCourtDetails;
    }

    public Map<String, Object> run(Map<String, Object> payload) throws WorkflowException {

        return this.execute(new Task[] {
            setCourtDetails,
        }, payload);
    }
}
