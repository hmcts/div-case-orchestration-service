package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateBulkCourtHearingDate;

import java.util.Map;

@Component
public class ValidateBulkCaseListingWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private ValidateBulkCourtHearingDate validateBulkCourtHearingDate;

    public Map<String, Object> run(Map<String, Object> bulkCaseData) throws WorkflowException {
        return this.execute(
                new Task[] {
                    validateBulkCourtHearingDate
                },
                bulkCaseData
        );

    }

}
