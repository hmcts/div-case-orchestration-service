package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.RetryableBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseIdFromCaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@AllArgsConstructor
public class LinkBulkCaseWorkflow  extends RetryableBulkCaseWorkflow {

    static final String LINK_BULK_CASE_EVENT = "linkBulkCaseReference";

    private final UpdateCaseInCCD updateCaseInCCD;
    private final GetCaseIdFromCaseLink getCaseIDFromCaseLink;

    public Map<String, Object> run(Map<String, Object> bulkCaseData,
                                   String caseId,
                                   String authToken) throws WorkflowException {

        return this.execute(
            new Task[] {
                getCaseIDFromCaseLink,
                updateCaseInCCD
            },
            bulkCaseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, LINK_BULK_CASE_EVENT)
        );
    }
}
