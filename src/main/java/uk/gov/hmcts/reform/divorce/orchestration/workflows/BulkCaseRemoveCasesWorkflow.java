package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SyncBulkCaseListTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDivorceCasesWithinBulkTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_DETAILS_CONTEXT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@AllArgsConstructor
public class BulkCaseRemoveCasesWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SyncBulkCaseListTask syncBulkCaseListTask;
    private final UpdateDivorceCasesWithinBulkTask updateDivorceCasesWithinBulkTask;

    public Map<String, Object> run(CaseDetails bulkCaseDetails, String authToken) throws WorkflowException {
        return this.execute(
                new Task[] {
                    syncBulkCaseListTask,
                    updateDivorceCasesWithinBulkTask
                },
                bulkCaseDetails.getCaseData(),
                ImmutablePair.of(BULK_CASE_DETAILS_CONTEXT_KEY, bulkCaseDetails),
                ImmutablePair.of(CASE_ID_JSON_KEY, bulkCaseDetails.getCaseId()),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }
}
