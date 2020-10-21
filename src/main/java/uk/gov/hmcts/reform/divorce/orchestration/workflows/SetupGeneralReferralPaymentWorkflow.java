package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetGeneralReferralApplicationFeeTask;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class SetupGeneralReferralPaymentWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GetGeneralReferralApplicationFeeTask getGeneralReferralApplicationFeeTask;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return this.execute(
            new Task[] {
                getGeneralReferralApplicationFeeTask
            },
            ccdCallbackRequest.getCaseDetails().getCaseData()
        );
    }
}