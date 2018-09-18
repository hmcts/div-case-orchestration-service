package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_FEE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;

@Component
public class SetOrderSummary implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        // Throw exception if missing fee
        if (Objects.isNull(context.getTransientObject(PETITION_ISSUE_FEE_JSON_KEY))) {
            throw new TaskException("Missing fee data");
        }

        // Building the order summary is currently done here
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add((FeeResponse) context.getTransientObject(PETITION_ISSUE_FEE_JSON_KEY));
        caseData.put(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary);
        return caseData;
    }
}