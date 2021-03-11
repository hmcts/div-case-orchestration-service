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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;

@Component
@Slf4j
public class MigrateChequeTask implements Task<Map<String, Object>> {

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context,  Map<String, Object> payload) {

        log.debug("DEBUG Payment Value (should be 'cheque'): {}", payload.get(SOLICITOR_HOW_TO_PAY_JSON_KEY));

        payload.replace(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);

        log.debug("DEBUG Payment Value (should be 'feePayByAccount'): {}", payload.get(SOLICITOR_HOW_TO_PAY_JSON_KEY));

        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String eventId = context.getTransientObject(CASE_EVENT_ID_JSON_KEY);

        log.debug("DEBUG Auth Token: {}", authToken);
        log.debug("DEBUG Case ID: {}", caseId);
        log.debug("DEBUG Event ID: {}", eventId);

        try {
            payload = caseMaintenanceClient.updateCase(
                authToken,
                caseId,
                eventId,
                payload
            );
        } catch (FeignException e) {

            log.error("Case id {}: Update failed for payment cheque migration. Error: {}", caseId, e.getMessage());

            throw new TaskException("Case update failed!");
        }

        log.info("Case id {}: Migrated payment method", caseId);

        return payload;
    }

}