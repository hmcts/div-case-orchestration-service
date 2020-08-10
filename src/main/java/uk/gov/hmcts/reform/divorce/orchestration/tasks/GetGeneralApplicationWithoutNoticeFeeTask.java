package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetGeneralApplicationWithoutNoticeFeeTask implements Task<Map<String, Object>> {

    private final FeesAndPaymentsClient feesAndPaymentsClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feesAndPaymentsClient.getGeneralApplicationWithoutFee());

        caseData.put("paymentSummary", orderSummary);

        return caseData;
    }

}