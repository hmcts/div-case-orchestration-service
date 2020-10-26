package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.GetBailiffApplicationFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.GetGeneralApplicationWithoutNoticeFeeTask;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RequiredArgsConstructor
@Component
public class SetupConfirmServicePaymentWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GetGeneralApplicationWithoutNoticeFeeTask getGeneralApplicationWithoutNoticeFeeTask;
    private final GetBailiffApplicationFeeTask getBailiffApplicationFeeTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        return this.execute(
            new Task[] {
                Conditions.isServiceApplicationBailiff(caseDetails.getCaseData()) ? getBailiffApplicationFeeTask
                    : getGeneralApplicationWithoutNoticeFeeTask
            },
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId())
        );
    }
}