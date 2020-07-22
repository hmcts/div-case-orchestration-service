package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ReceivedServiceAddedDateTask;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_AMENDED_PETITION_DRAFT_KEY;

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
