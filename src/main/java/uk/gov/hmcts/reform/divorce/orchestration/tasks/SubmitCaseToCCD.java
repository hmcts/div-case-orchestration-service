package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

@Component
public class SubmitCaseToCCD implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public SubmitCaseToCCD(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }


    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> caseData,
                                       Object... params) {
        return caseMaintenanceClient.submitCase(caseData, String.valueOf(params[0]));
    }
}
