package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FeeLookupWithoutNoticeTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_FEE_AMOUNT_FIELD;

@Component
public class GetGeneralReferralApplicationFeeTask extends FeeLookupWithoutNoticeTask {

    public GetGeneralReferralApplicationFeeTask(FeesAndPaymentsClient feesAndPaymentsClient) {
        super(feesAndPaymentsClient);
    }

    @Override
    public String getOrderSummaryFieldName() {
        return CcdFields.GENERAL_REFERRAL_WITHOUT_NOTICE_FEE_SUMMARY;
    }

    @Override
    protected OrderSummary getOrderSummary() {
        return super.getOrderSummary();
    }

    @Override
    public String getFeeValueFieldName() {
        return GENERAL_REFERRAL_FEE_AMOUNT_FIELD;
    }
}
