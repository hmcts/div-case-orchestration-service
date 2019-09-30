package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveCostOrderDocumentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveDecreeNisiDocumentTask;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RemoveDNDocumentsWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final RemoveDecreeNisiDocumentTask removeDecreeNisiDocumentTask;
    private final RemoveCostOrderDocumentTask removeCostOrderDocumentTask;

    public Map<String, Object> run(Map<String, Object> caseData) throws WorkflowException {

        return this.execute(
            new Task[] {
                removeDecreeNisiDocumentTask,
                removeCostOrderDocumentTask
            },
            caseData
        );
    }
}
