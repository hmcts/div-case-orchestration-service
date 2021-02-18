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

        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);

        payload.replace(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);

        try {
            payload = caseMaintenanceClient.updateCase(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                caseId,
                context.getTransientObject(CASE_EVENT_ID_JSON_KEY),
                payload
            );
        } catch (FeignException e) {

            log.error("Case id {}: Update failed for payment cheque migration", caseId, e);

            throw new TaskException("Case update failed!", e);
        }

        log.info("Case id {}: Migrated payment method", caseId);

        return payload;
    }

}