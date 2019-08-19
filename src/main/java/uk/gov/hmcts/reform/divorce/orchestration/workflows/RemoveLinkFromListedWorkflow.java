package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveCertificateOfEntitlementDocumentsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveListingDataTask;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RemoveLinkFromListedWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final RemoveListingDataTask removeListingDataTask;
    private final RemoveCertificateOfEntitlementDocumentsTask removeCertificateOfEntitlementDocumentsTask;

    public Map<String, Object> run(Map<String, Object> caseData) throws WorkflowException {

        return this.execute(
            new Task[] {
                removeListingDataTask,
                removeCertificateOfEntitlementDocumentsTask
            },
            caseData
        );
    }
}
