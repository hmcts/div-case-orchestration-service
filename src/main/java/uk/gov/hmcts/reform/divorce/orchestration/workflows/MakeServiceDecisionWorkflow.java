package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MakeServiceDecisionTask;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MakeServiceDecisionWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final MakeServiceDecisionTask makeServiceDecisionTask;

    public Map<String, Object> run(Map<String, Object> caseData) throws WorkflowException {

        return this.execute(
            new Task[] {
                makeServiceDecisionTask
            },
            caseData
        );
    }
}
