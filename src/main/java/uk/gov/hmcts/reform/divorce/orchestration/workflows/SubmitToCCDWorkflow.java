package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CourtAllocationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraft;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DuplicateCaseValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.ALLOCATED_COURT_KEY;

@Slf4j
@Component
public class SubmitToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    public static final String SELECTED_COURT = "selectedCourt";

    @Autowired
    private DuplicateCaseValidationTask duplicateCaseValidationTask;

    @Autowired
    private CourtAllocationTask courtAllocationTask;

    @Autowired
    private FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Autowired
    private ValidateCaseData validateCaseData;

    @Autowired
    private SubmitCaseToCCD submitCaseToCCD;

    @Autowired
    private DeleteDraft deleteDraft;

    public Map<String, Object> run(Map<String, Object> payload, String authToken) throws WorkflowException {
        Map<String, Object> returnFromExecution = this.execute(
            new Task[]{
                duplicateCaseValidationTask,
                courtAllocationTask,
                formatDivorceSessionToCaseData,
                validateCaseData,
                submitCaseToCCD,
                deleteDraft
            },
            payload,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );

        Map<String, Object> response = new HashMap<>(returnFromExecution);
        Court selectedCourt = getContext().getTransientObject(SELECTED_COURT);
        response.put(ALLOCATED_COURT_KEY, selectedCourt);

        String caseId = String.valueOf(returnFromExecution.get(ID));
        log.info("Allocated case with CASE ID: {} to court: {}", caseId, selectedCourt.getCourtId());

        return response;
    }

}