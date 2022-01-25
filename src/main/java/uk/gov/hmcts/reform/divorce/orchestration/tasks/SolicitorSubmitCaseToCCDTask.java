package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Component
@Slf4j
public class SolicitorSubmitCaseToCCDTask implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public SolicitorSubmitCaseToCCDTask(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String oldCaseId = context.getTransientObject(CASE_ID_JSON_KEY);
        log.info("Submitting amended caseData to CCD for old case id {}", oldCaseId);

        Map<String, Object> updatedCaseData = caseMaintenanceClient.solicitorSubmitCase(
                caseData,
                context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString()
        );

        String newCaseId = updatedCaseData.getOrDefault(ID, "").toString();
        log.info("Created amended case, new case Id {} from old case id {}", newCaseId, oldCaseId);

        return updatedCaseData;
    }
}
