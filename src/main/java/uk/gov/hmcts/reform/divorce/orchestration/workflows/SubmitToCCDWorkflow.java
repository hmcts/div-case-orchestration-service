package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CourtAllocationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraft;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DuplicateCaseValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Slf4j
@Component
public class SubmitToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    public static final String SELECTED_COURT = "selectedCourt";

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

    @Autowired
    private DuplicateCaseValidationTask duplicateCaseValidationTask;

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

        return returnFromExecution;
    }

}