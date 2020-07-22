package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ReceivedServiceAddedDateTask;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReceivedServiceAddedDateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ReceivedServiceAddedDateTask receivedServiceAddedDateTask;

    public Map<String, Object> run(Map<String, Object> caseData) throws WorkflowException {
        this.execute(
            new Task[] {
                receivedServiceAddedDateTask
            },
            caseData
        );

        return caseData;
    }
}
