package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class JudgeHelperTest {

    @Test
    public void isJudgeGrantCostOrderYesReturnsTrue_WhenJudgeCostsClaimGrantedIsYesValue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(JUDGE_COSTS_CLAIM_GRANTED, YES_VALUE);

        assertThat(JudgeHelper.isJudgeGrantCostOrderYes(caseData), is(true));
    }

    @Test
    public void isJudgeGrantCostOrderYesReturnsTrue_WhenJudgeCostsClaimGrantedIsNoValue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(JUDGE_COSTS_CLAIM_GRANTED, NO_VALUE);

        assertThat(JudgeHelper.isJudgeGrantCostOrderYes(caseData), is(false));
    }

    @Test
    public void isJudgeGrantCostOrderYesReturnsTrue_WhenJudgeCostsClaimGrantedIsNotYesValue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(JUDGE_COSTS_CLAIM_GRANTED, "Adjourn");

        assertThat(JudgeHelper.isJudgeGrantCostOrderYes(caseData), is(false));
    }


}
