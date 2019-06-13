package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateBulkCaseInCcd;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class UpdateBulkCaseWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private UpdateBulkCaseInCcd updateBulkCaseInCcd;

    public Map<String, Object> run(Map<String, Object> payload,
                                   String authToken,
                                   String caseId,
                                   String eventId) throws WorkflowException {

        return this.execute(
                new Task[] {
                    updateBulkCaseInCcd
                },
                payload,
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
                ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, eventId)
        );
    }
}
