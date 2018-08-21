package uk.gov.hmcts.reform.divorce.orchestration.workflows;

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

@Component
@Scope("prototype")
public class SubmitToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Autowired
    private ValidateCaseData validateCaseData;

    @Autowired
    private SubmitCaseToCCD submitCaseToCCD;

    public Map<String, Object> run(Map<String, Object> payload, String authToken) throws WorkflowException {
        
        formatDivorceSessionToCaseData.setup(authToken);
        submitCaseToCCD.setup(authToken);

        return this.execute(new Task[] {
            formatDivorceSessionToCaseData,
            validateCaseData,
            submitCaseToCCD
        }, payload);
    }
}
