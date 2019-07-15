package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER_EVENT_ID;

@Component
public class MakeCaseEligibleForDecreeAbsoluteWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private UpdateCaseInCCD updateCaseInCCD;

    public MakeCaseEligibleForDecreeAbsoluteWorkflow(UpdateCaseInCCD updateCaseInCCD) {
        this.updateCaseInCCD = updateCaseInCCD;
    }

    public Map<String, Object> run(String authToken, String caseId) throws WorkflowException {
        return this.execute(
            new Task[] {
                updateCaseInCCD
            },
            emptyMap(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER_EVENT_ID)
        );
    }

}
