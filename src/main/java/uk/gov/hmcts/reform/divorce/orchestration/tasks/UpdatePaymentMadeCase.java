package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_MADE_EVENT;

@Component
@Slf4j
public class UpdatePaymentMadeCase implements Task<Map<String, Object>> {

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
        String caseState = context.getTransientObject(CASE_STATE_JSON_KEY);
        Map<String, Object> caseResponse = null;
        if (AWAITING_PAYMENT.equalsIgnoreCase(caseState) && caseData.containsKey(D_8_PAYMENTS)) {
            context.setTransientObject(CASE_EVENT_ID_JSON_KEY, PAYMENT_MADE_EVENT);
            caseResponse = caseMaintenanceClient.updateCase(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                context.getTransientObject(CASE_ID_JSON_KEY),
                context.getTransientObject(CASE_EVENT_ID_JSON_KEY),
                caseData
            );
            log.info("Case id {} updated with {} event", caseId, PAYMENT_MADE_EVENT);
        }

        return caseResponse;
    }
}
