package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithIdTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidatedCaseLinkTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LINK_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@AllArgsConstructor
public class RemoveBulkCaseLinkWorkflow extends DefaultWorkflow<Map<String, Object>> {

    static final String UN_LINK_BULK_CASE_EVENT = "unlinkBulkCaseReference";

    private final UpdateCaseInCCD updateCaseInCCD;
    private final GetCaseWithIdTask getCaseWithId;
    private final ValidatedCaseLinkTask validateBulkCaseLinkTask;

    public Map<String, Object> run(Map<String, Object> bulkCaseData,
                                   String caseId,
                                   String bulkCaseId,
                                   String authToken) throws WorkflowException {

        return this.execute(
            new Task[] {
                getCaseWithId,
                validateBulkCaseLinkTask,
                updateCaseInCCD
            },
            bulkCaseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(BULK_LINK_CASE_ID, bulkCaseId),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, UN_LINK_BULK_CASE_EVENT)
        );
    }
}
