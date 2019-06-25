package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.RetryableBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDnGrantedDetailsFromBulkCase;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_DETAILS_CONTEXT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_BULK_DN_PRONOUNCEMENT_DETAILS_EVENT;

@Component
@AllArgsConstructor
public class UpdatePronouncementDateWorkflow extends RetryableBulkCaseWorkflow {

    private final SetDnGrantedDetailsFromBulkCase setDnGrantedDetailsFromBulkCase;
    private final UpdateCaseInCCD updateCaseInCCD;

    public Map<String, Object> run(Map<String, Object> bulkCaseDetails,
                                   String caseId,
                                   String authToken) throws WorkflowException {

        return this.execute(
                new Task[] {
                    setDnGrantedDetailsFromBulkCase,
                    updateCaseInCCD
                },
                new HashMap<>(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(BULK_CASE_DETAILS_CONTEXT_KEY, bulkCaseDetails),
                ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, UPDATE_BULK_DN_PRONOUNCEMENT_DETAILS_EVENT),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
