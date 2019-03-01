package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToAosCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitRespondentAosCase;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class SubmitRespondentAosCaseWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final FormatDivorceSessionToAosCaseData formatDivorceSessionToAosCaseData;

    private final SubmitRespondentAosCase submitRespondentAosCase;

    @Autowired
    public SubmitRespondentAosCaseWorkflow(final FormatDivorceSessionToAosCaseData formatDivorceSessionToAosCaseData,
                                           final SubmitRespondentAosCase submitRespondentAosCase) {
        this.formatDivorceSessionToAosCaseData = formatDivorceSessionToAosCaseData;
        this.submitRespondentAosCase = submitRespondentAosCase;
    }

    public Map<String, Object> run(Map<String, Object> payload, String authToken, String caseId) throws WorkflowException {
        return this.execute(
            new Task[] {
                formatDivorceSessionToAosCaseData,
                submitRespondentAosCase
            },
            payload,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
