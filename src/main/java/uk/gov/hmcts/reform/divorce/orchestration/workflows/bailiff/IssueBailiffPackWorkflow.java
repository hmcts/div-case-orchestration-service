package uk.gov.hmcts.reform.divorce.orchestration.workflows.bailiff;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bailiff.CertificateOfServiceGenerationTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@RequiredArgsConstructor
public class IssueBailiffPackWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final CertificateOfServiceGenerationTask certificateOfServiceGenerationTask;

    public Map<String, Object> issueCertificateOfServiceDocument(String authToken, String caseId, Map<String, Object> caseData)
        throws WorkflowException {
        return execute(new Task[] {certificateOfServiceGenerationTask},
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }
}
