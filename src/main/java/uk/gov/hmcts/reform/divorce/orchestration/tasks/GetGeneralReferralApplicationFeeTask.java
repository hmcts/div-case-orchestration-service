package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FeeLookupWithoutNoticeTask;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_WITHOUT_NOTICE_FEE_SUMMARY;

@Component
@Slf4j
public class GetGeneralReferralApplicationFeeTask extends FeeLookupWithoutNoticeTask {

    public GetGeneralReferralApplicationFeeTask(FeesAndPaymentsClient feesAndPaymentsClient) {
        super(feesAndPaymentsClient);
    }

    @Override
    protected String getFieldName() {
        return GENERAL_REFERRAL_WITHOUT_NOTICE_FEE_SUMMARY;
    }
}
