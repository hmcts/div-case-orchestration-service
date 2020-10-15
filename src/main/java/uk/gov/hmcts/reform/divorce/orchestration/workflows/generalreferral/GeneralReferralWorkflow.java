package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralReferralWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GeneralReferralTask generalReferralTask;

    public Map<String, Object> run(Map<String, Object> caseData, String auth) throws WorkflowException {

        return this.execute(
            new Task[] {
                generalReferralTask
            },
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, auth)
        );
    }
}
