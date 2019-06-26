package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;

@Component
@AllArgsConstructor
public class CleanStateFromCaseDataWorkflow extends DefaultWorkflow<Map<String, Object>>  {

    private static final String CLEAN_CASE_STATE_EVENT = "cleanCaseState";

    private final UpdateCaseInCCD updateCaseInCCD;

    public Map<String, Object> run(String caseId, String authToken) throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(STATE_CCD_FIELD, null);
        return this.execute(
                new Task[] {
                    updateCaseInCCD
                },
                caseData,
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
                ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, CLEAN_CASE_STATE_EVENT)
        );
    }
}
