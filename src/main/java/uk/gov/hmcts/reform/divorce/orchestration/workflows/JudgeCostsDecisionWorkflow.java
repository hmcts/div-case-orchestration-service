package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddJudgeCostsDecisionToPayloadTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.JudgeCostsClaimFieldsRemovalTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_COSTS_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.JudgeHelper.isJudgeGrantCostOrderYes;

@Component
@Slf4j
@RequiredArgsConstructor
public class JudgeCostsDecisionWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final JudgeCostsClaimFieldsRemovalTask judgeCostsClaimFieldsRemovalTask;
    private final AddJudgeCostsDecisionToPayloadTask addJudgeCostsDecisionToPayloadTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        return this.execute(
            getTasks(caseDetails),
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId())
        );
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        final String caseId = caseDetails.getCaseId();

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        log.info("CaseID: {} Adding field {} to payload with `Yes` value.", caseId, JUDGE_COSTS_DECISION);
        tasks.add(addJudgeCostsDecisionToPayloadTask);

        if (!isJudgeGrantCostOrderYes(caseDetails.getCaseData())) {
            log.info("CaseId: {}, cleaning Judge Costs Claim fields", caseId);
            tasks.add(judgeCostsClaimFieldsRemovalTask);
        }

        return tasks.toArray(new Task[] {});
    }

}
