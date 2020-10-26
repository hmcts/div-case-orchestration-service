package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralApplicationFeeTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RequiredArgsConstructor
@Component
public class SetupGeneralReferralPaymentWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GeneralReferralApplicationFeeTask generalReferralApplicationFeeTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        return this.execute(
            new Task[] {
                generalReferralApplicationFeeTask
            },
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId())
        );
    }
}