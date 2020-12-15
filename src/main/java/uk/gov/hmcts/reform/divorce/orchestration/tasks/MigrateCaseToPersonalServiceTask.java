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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PERSONAL_SERVICE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_SERVICE_METHOD_CCD_FIELD;

@Slf4j
@Component
public class MigrateCaseToPersonalServiceTask implements Task<Map<String, Object>> {

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        final String caseId = context.getTransientObject(CASE_ID_JSON_KEY);


        log.info("Updating service to personalService - Case ID: {}", caseId);
        payload.replace(SOL_SERVICE_METHOD_CCD_FIELD, PERSONAL_SERVICE_VALUE);
        try {
            payload = caseMaintenanceClient.updateCase(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                context.getTransientObject(CASE_ID_JSON_KEY),
                context.getTransientObject(CASE_EVENT_ID_JSON_KEY),
                payload
            );
            log.info("CMC update case completed. Updated {} to value: {}",
                SOL_SERVICE_METHOD_CCD_FIELD, payload.get(SOL_SERVICE_METHOD_CCD_FIELD));
        } catch (FeignException exception) {
            throw new TaskException("Case update failed", exception);
        }
        log.info("returning payload from MigrateCaseToPersonalService Task");
        return payload;
    }
}
