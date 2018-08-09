package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class SubmitToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    private final ValidateCaseData validateCaseData;

    private final SubmitCaseToCCD submitCaseToCCD;

    @Autowired
    public SubmitToCCDWorkflow(FormatDivorceSessionToCaseData formatDivorceSessionToCaseData,
                               ValidateCaseData validateCaseData,
                               SubmitCaseToCCD submitCaseToCCD) {
        this.formatDivorceSessionToCaseData = formatDivorceSessionToCaseData;
        this.validateCaseData = validateCaseData;
        this.submitCaseToCCD = submitCaseToCCD;
    }

    public Map<String, Object> run(Map<String, Object> payload, String authToken) throws WorkflowException {

        return this.execute(new Task[] {
            formatDivorceSessionToCaseData,
            validateCaseData,
            submitCaseToCCD
        }, payload,
            new ImmutablePair(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }
}
