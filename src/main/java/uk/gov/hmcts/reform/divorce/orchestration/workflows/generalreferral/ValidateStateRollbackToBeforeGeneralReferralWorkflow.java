package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.VerifyIsEligibleForStateRollbackBeforeGeneralReferralTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidateStateRollbackToBeforeGeneralReferralWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final VerifyIsEligibleForStateRollbackBeforeGeneralReferralTask verifyIsEligibleForStateRollbackToBeforeGeneralReferralTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {

        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} Validate state rollback before general referral workflow is going to be executed.", caseId);

        return this.execute(
            new Task[] {
                verifyIsEligibleForStateRollbackToBeforeGeneralReferralTask
            },
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
