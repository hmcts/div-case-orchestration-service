package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_SUBMITTED_CASE_KEY;

@Component
public class SolicitorSubmitCaseToCCDTask implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public SolicitorSubmitCaseToCCDTask(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        final Map<String, Object> submittedCase = caseMaintenanceClient.solicitorSubmitCase(
                caseData,
                context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString()
        );

        context.setTransientObject(NEW_SUBMITTED_CASE_KEY, submittedCase);
        // return empty as next step (update case state) needs no data (empty)
        return new HashMap<>();
    }
}
