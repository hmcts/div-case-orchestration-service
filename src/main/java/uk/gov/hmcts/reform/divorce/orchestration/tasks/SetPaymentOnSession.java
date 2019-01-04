package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EXISTING_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_PAYMENT_STATUS;

@Component
@Slf4j
public class SetPaymentOnSession implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = String.valueOf(context.getTransientObject(CASE_ID_JSON_KEY));
        Stream<Map<String, Object>> paymentMaps = Optional
                .ofNullable((List<Map<String, Object>>)caseData.get(EXISTING_PAYMENTS))
                .orElse(Collections.emptyList())
                .stream();

        paymentMaps.filter(payment ->
                SUCCESS_PAYMENT_STATUS.equalsIgnoreCase(String.valueOf(payment.get(PAYMENT_STATUS))))
                .findFirst()
                .ifPresent(successPayment -> {
                    log.info("Case Id {} has successful payment with ref {}", caseId,
                            successPayment.get(PAYMENT_REFERENCE));
                    caseData.put(PAYMENT_REFERENCE, successPayment.get(PAYMENT_REFERENCE));
                });
        return caseData;
    }

}