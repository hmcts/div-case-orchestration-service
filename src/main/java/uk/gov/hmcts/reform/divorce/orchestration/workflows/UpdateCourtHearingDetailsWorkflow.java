package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.RetryableBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithIdMapFlow;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetCourtHearingDetailsFromBulkCase;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_COURT_HEARING_DETAILS_EVENT;

@Component
@AllArgsConstructor
public class UpdateCourtHearingDetailsWorkflow extends RetryableBulkCaseWorkflow {

    private final GetCaseWithIdMapFlow getCaseWithIdMapFlow;
    private final SetCourtHearingDetailsFromBulkCase setCourtHearingDetailsFromBulkCase;
    private final UpdateCaseInCCD updateCaseInCCD;

    public Map<String, Object> run(Map<String, Object> bulkCaseData,
                                   String caseId,
                                   String authToken) throws WorkflowException {

        return this.execute(
                new Task[] {
                    getCaseWithIdMapFlow,
                    setCourtHearingDetailsFromBulkCase,
                    updateCaseInCCD
                },
                bulkCaseData,
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, UPDATE_COURT_HEARING_DETAILS_EVENT),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
