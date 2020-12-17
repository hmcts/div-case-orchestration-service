package uk.gov.hmcts.reform.divorce.orchestration.workflows.aos;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aos.MarkCasesAsAosOverdueTask;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
@RequiredArgsConstructor
public class AosOverdueEligibilityWorkflow extends DefaultWorkflow<Void> {

    private final MarkCasesAsAosOverdueTask markCasesToBeMovedToAosOverdue;

    public void run(String authToken) throws WorkflowException {
        execute(new Task[] {markCasesToBeMovedToAosOverdue}, null, ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken));
    }

}