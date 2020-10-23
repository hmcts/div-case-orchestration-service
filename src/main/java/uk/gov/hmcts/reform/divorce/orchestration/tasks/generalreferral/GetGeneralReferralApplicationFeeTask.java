package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FeeLookupWithoutNoticeTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public class GetGeneralReferralApplicationFeeTask extends FeeLookupWithoutNoticeTask {

    public GetGeneralReferralApplicationFeeTask(FeesAndPaymentsClient feesAndPaymentsClient) {
        super(feesAndPaymentsClient);
    }

    @Override
    protected Map<String, Object> updateCaseData(TaskContext context, Map<String, Object> caseData) {
        final String caseId = getCaseId(context);

        Map<String, Object> updatedCaseData = super.updateCaseData(context, caseData);

        String feeValue = getFeeValue(updatedCaseData);

        log.info("CaseId: {}, populate field {} with fee value from SummaryOrder (in pennies)", caseId, feeValue);

        updatedCaseData.put(CcdFields.FEE_AMOUNT_WITHOUT_NOTICE, feeValue);

        return updatedCaseData;
    }

    private String getFeeValue(Map<String, Object> caseData) {
        OrderSummary orderSummary = (OrderSummary) caseData.get(getFieldName());

        return orderSummary.getPaymentTotalInPounds();
    }

    @Override
    public String getFieldName() {
        return CcdFields.GENERAL_REFERRAL_WITHOUT_NOTICE_FEE_SUMMARY;
    }
}
