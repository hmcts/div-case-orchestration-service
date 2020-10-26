package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class FeeLookupWithoutNoticeTask implements Task<Map<String, Object>> {
    private final FeesAndPaymentsClient feesAndPaymentsClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        log.info(
            "CaseId: {}, getting general application without notice fee for field {}",
            getCaseId(context),
            getFieldName()
        );

        return updateCaseData(context, caseData);
    }

    protected Map<String, Object> updateCaseData(TaskContext context, Map<String, Object> caseData) {
        return updateOrderSummary(context, caseData);
    }

    protected Map<String, Object> updateOrderSummary(TaskContext context, Map<String, Object> caseData) {
        final String fieldName = getFieldName();
        final String caseId = getCaseId(context);

        log.info("CaseId: {}, getting general application without notice fee for field {}", caseId, fieldName);

        OrderSummary orderSummary = getOrderSummary();
        log.info("CaseId: {}, fee: {}", caseId, orderSummary.getFees().get(0));

        caseData.put(fieldName, orderSummary);

        return caseData;
    }

    protected OrderSummary getOrderSummary() {
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feesAndPaymentsClient.getGeneralApplicationWithoutFee());

        return orderSummary;
    }

    public abstract String getFieldName();
}
