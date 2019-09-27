package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.RetryableBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_DETAILS_CONTEXT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@AllArgsConstructor
public class UpdateCaseWithinBulkWorkflow extends RetryableBulkCaseWorkflow {

    private static final String CANCEL_PRONOUNCEMENT_EVENT = "cancelPronouncement";

    private final UpdateCaseInCCD updateCaseInCCD;

    public Map<String, Object> run(Map<String, Object> bulkCaseDetails,
                                   String caseId,
                                   String authToken) throws WorkflowException {

        return this.execute(
                new Task[] {
                    updateCaseInCCD
                },
                emptyMap(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(BULK_CASE_DETAILS_CONTEXT_KEY, bulkCaseDetails),
                ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, CANCEL_PRONOUNCEMENT_EVENT),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
