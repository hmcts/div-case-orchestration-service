package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FeeLookupWithoutNoticeTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY;

@Component
@Slf4j
public class GetGeneralApplicationWithoutNoticeFeeTask extends FeeLookupWithoutNoticeTask {

    public GetGeneralApplicationWithoutNoticeFeeTask(FeesAndPaymentsClient feesAndPaymentsClient) {
        super(feesAndPaymentsClient);
    }

    @Override
    protected Map<String, Object> updateCaseData(TaskContext context, Map<String, Object> caseData) {
        return updateOrderSummary(context, caseData);
    }

    @Override
    public String getFieldName() {
        return GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY;
    }
}