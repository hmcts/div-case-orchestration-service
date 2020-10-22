package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FeeLookupWithoutNoticeTask;

@Component
public class GetGeneralReferralApplicationFeeTask extends FeeLookupWithoutNoticeTask {

    public GetGeneralReferralApplicationFeeTask(FeesAndPaymentsClient feesAndPaymentsClient) {
        super(feesAndPaymentsClient);
    }

    @Override
    public String getFieldName() {
        return CcdFields.GENERAL_REFERRAL_WITHOUT_NOTICE_FEE_SUMMARY;
    }
}
