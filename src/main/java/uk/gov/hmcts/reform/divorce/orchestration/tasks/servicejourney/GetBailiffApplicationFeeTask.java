package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FeeLookupWithoutNoticeTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY;

@Component
public class GetBailiffApplicationFeeTask extends FeeLookupWithoutNoticeTask {

    public GetBailiffApplicationFeeTask(FeesAndPaymentsClient feesAndPaymentsClient) {
        super(feesAndPaymentsClient);
    }

    @Override
    public String getOrderSummaryFieldName() {
        return GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY;
    }

    @Override
    protected OrderSummary getOrderSummary() {
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feesAndPaymentsClient.getBailiffApplicationFee());

        return orderSummary;
    }

    @Override
    protected Map<String, Object> furtherUpdateCaseData(TaskContext context, Map<String, Object> updatedCaseData) {
        return updatedCaseData;
    }
}
