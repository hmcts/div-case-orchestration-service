package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.WelshContinueTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@RequiredArgsConstructor
public class WelshContinueWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final WelshContinueTask welshContinueTask;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest,
                                   String authToken) throws WorkflowException {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        return this.execute(
            new Task[] {
                welshContinueTask
            },
            ccdCallbackRequest.getCaseDetails().getCaseData(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
