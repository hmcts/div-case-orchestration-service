package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SetupOrderSummaryTask;

@Component
public class GetBailiffApplicationFeeTask extends SetupOrderSummaryTask {

    public GetBailiffApplicationFeeTask(FeesAndPaymentsClient feesAndPaymentsClient) {
        super(feesAndPaymentsClient);
    }

    @Override
    protected String getFieldName() {
        return CcdFields.GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY;
    }

    @Override
    protected FeeResponse getFeeResponse() {
        return feesAndPaymentsClient.getBailiffApplicationFee();
    }
}