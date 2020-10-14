package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@RequiredArgsConstructor
@Slf4j
public abstract class SetupOrderSummaryTask implements Task<Map<String, Object>> {

    protected final FeesAndPaymentsClient feesAndPaymentsClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = getCaseId(context);
        String field = getFieldName();

        log.info("CaseId: {} setting up a field: {}", caseId, field);

        return setupField(caseData);
    }

    protected abstract String getFieldName();

    protected abstract FeeResponse getFeeResponse();

    protected Map<String, Object> setupField(Map<String, Object> caseData) {
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(getFeeResponse());

        caseData.put(getFieldName(), orderSummary);

        return caseData;
    }
}