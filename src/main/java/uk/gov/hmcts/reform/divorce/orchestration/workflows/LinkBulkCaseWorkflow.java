package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseIdFromCaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;

@Component
@AllArgsConstructor
public class LinkBulkCaseWorkflow  extends DefaultWorkflow<Map<String, Object>> {

    private static final String LINK_BULK_CASE_EVENT = "LinkBulkCaseReference";

    private final UpdateCaseInCCD updateCaseInCCD;
    private final GetCaseIdFromCaseLink getCaseIDFromCaseLink;

    public Map<String, Object> run(Map<String, Object> caseData,
                                   String bulkCaseId,
                                   String authToken) throws WorkflowException {


        caseData.put(BULK_LISTING_CASE_ID_FIELD, bulkCaseId);
        return this.execute(
            new Task[] {
                getCaseIDFromCaseLink,
                updateCaseInCCD
            },
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(BULK_LISTING_CASE_ID_FIELD, bulkCaseId),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, LINK_BULK_CASE_EVENT)
        );
    }
}
