package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralApplicationAddedDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralSetPreviousCaseStateTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralReferralWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GeneralApplicationAddedDateTask generalApplicationAddedDateTask;
    private final GeneralReferralSetPreviousCaseStateTask generalReferralSetPreviousCaseStateTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {

        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} general referral workflow is going to be executed.", caseId);

        return this.execute(
            new Task[] {
                generalApplicationAddedDateTask,
                generalReferralSetPreviousCaseStateTask
            },
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(CASE_STATE_JSON_KEY, caseDetails.getState())
        );
    }
}
