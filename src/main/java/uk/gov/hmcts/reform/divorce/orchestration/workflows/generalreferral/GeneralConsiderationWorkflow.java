package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDecisionDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralFieldsRemovalTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralConsiderationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GeneralReferralDecisionDateTask generalReferralDecisionDateTask;
    private final GeneralReferralDataTask generalReferralDataTask;
    private final GeneralReferralFieldsRemovalTask generalReferralFieldsRemovalTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {

        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} general consideration workflow is going to be executed.", caseId);

        return this.execute(
            new Task[] {
                generalReferralDecisionDateTask,
                generalReferralDataTask,
                generalReferralFieldsRemovalTask
            },
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
