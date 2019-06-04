package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithId;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetCourtHearingDetailsFromBulkCase;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_COURT_HEARING_DETAILS_EVENT;

@Component
@AllArgsConstructor
public class UpdateCourtHearingDetailsWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GetCaseWithId getCaseWithId;
    private final SetCourtHearingDetailsFromBulkCase setCourtHearingDetailsFromBulkCase;
    private final UpdateCaseInCCD updateCaseInCCD;

    public Map<String, Object> run(Map<String, Object> bulkCaseData,
                                   String caseId,
                                   String authToken) throws WorkflowException {

        UserDetails userDetails = UserDetails.builder().authToken(authToken).build();

        TaskContext retrieveCaseContext = new DefaultTaskContext();

        try {
            retrieveCaseContext.setTransientObject(CASE_ID_JSON_KEY, caseId);
            getCaseWithId.execute(retrieveCaseContext, userDetails);
        } catch (TaskException exception) {
            throw new WorkflowException(String.format(
                    "Unable to retrieve case for court hearing updates with case id %s", caseId), exception);
        }

        return this.execute(
                new Task[] {
                    setCourtHearingDetailsFromBulkCase,
                    updateCaseInCCD
                },
                bulkCaseData,
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, UPDATE_COURT_HEARING_DETAILS_EVENT),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
                ImmutablePair.of(CASE_DETAILS_JSON_KEY, retrieveCaseContext.getTransientObject(CASE_DETAILS_JSON_KEY))
        );
    }
}
