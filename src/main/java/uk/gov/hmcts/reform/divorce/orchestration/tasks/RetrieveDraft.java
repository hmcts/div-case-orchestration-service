package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.LinkedHashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

@Component
public class RetrieveDraft implements Task<Map<String, Object>> {
    private static final String CASE_DATA_KEY = "case_data";

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public RetrieveDraft(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }


    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> noPayLoad,
                                       Object... params) {

        Map<String, Object> cmsContent = caseMaintenanceClient.retrievePetition(String.valueOf(params[0]), true);
        if (cmsContent != null && cmsContent.containsKey(CASE_DATA_KEY)) {
            return (Map<String, Object>) cmsContent.get(CASE_DATA_KEY);
        } else {
            context.setTaskFailed(true);
            return new LinkedHashMap<>();
        }

    }
}
