package uk.gov.hmcts.reform.divorce.orchestration.workflows.aos;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aos.AosOverdueCoverLetterGenerationTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@RequiredArgsConstructor
public class AosNotReceivedWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final AosOverdueCoverLetterGenerationTask aosOverdueCoverLetterGenerationTask;

    public Map<String, Object> prepareForSubmission(String authToken, String caseId, Map<String, Object> caseData) throws WorkflowException {
        return execute(new Task[] {aosOverdueCoverLetterGenerationTask},
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }

}