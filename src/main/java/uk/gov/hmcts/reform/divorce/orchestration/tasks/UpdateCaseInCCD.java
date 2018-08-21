package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

@Component
public class UpdateCaseInCCD implements Task<Map<String, Object>> {

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        try {
            return caseMaintenanceClient.updateCase(
                    caseData,
                    (String) context.getTransientObject("authToken"),
                    (String) context.getTransientObject("caseId"),
                    (String) context.getTransientObject("eventId")
            );
        } catch (Exception exception) {
            throw new TaskException(exception.getMessage());
        }
    }
}
