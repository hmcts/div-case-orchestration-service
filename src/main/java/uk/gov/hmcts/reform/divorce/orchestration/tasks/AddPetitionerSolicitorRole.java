package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
public class AddPetitionerSolicitorRole implements Task<Map<String, Object>> {

    private CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public AddPetitionerSolicitorRole(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);

        try {
            caseMaintenanceClient.addPetitionerSolicitorRole(authToken, caseId);
            log.info("Role [PETSOLICITOR] is set for case ID: {}", caseId);
        } catch (final FeignException exception) {
            log.error("Problem setting the [PETSOLICITOR] role to the case: {}", caseId);
            context.setTaskFailed(true);
            context.setTransientObject(this.getClass().getName() + "_Error",
                "Problem setting the [PETSOLICITOR] role to the case");
        }
        return payload;
    }
}
