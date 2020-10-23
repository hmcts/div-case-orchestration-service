package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FeeLookupWithoutNoticeTask;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY;

@Component
@Slf4j
public class GetGeneralApplicationWithoutNoticeFeeTask extends FeeLookupWithoutNoticeTask {

    public GetGeneralApplicationWithoutNoticeFeeTask(FeesAndPaymentsClient feesAndPaymentsClient) {
        super(feesAndPaymentsClient);
    }

    @Override
    public String getOrderSummaryFieldName() {
        return GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY;
    }

    @Override
    public String getFeeValueFieldName() {
        return null;
    }
}