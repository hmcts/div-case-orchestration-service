package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_COSTS_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JudgeDecisionHelper {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)

    public static class JudgeDecisions {
        public static final String ADJOURN_VALUE = "Adjourn";
    }

    public static boolean isJudgeCostClaimGranted(Map<String, Object> caseData) {
        return isGranted(caseData, CcdFields.JUDGE_COSTS_CLAIM_GRANTED);
    }

    public static boolean isJudgeCostClaimRejected(Map<String, Object> caseData) {
        return isRejected(caseData, CcdFields.JUDGE_COSTS_CLAIM_GRANTED);
    }

    public static boolean isJudgeCostClaimAdjourned(Map<String, Object> caseData) {
        return isAdjourned(caseData, CcdFields.JUDGE_COSTS_CLAIM_GRANTED);
    }

    public static boolean isJudgeCostClaimEmpty(Map<String, Object> caseData) {
        String judgeDecision = (String) caseData.get(CcdFields.JUDGE_COSTS_CLAIM_GRANTED);

        return Strings.isNullOrEmpty(judgeDecision);
    }

    public static boolean hasJudgeGrantedCostsDecision(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(JUDGE_COSTS_DECISION)));
    }

    private static boolean isGranted(Map<String, Object> caseData, String field) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(field));
    }

    private static boolean isRejected(Map<String, Object> caseData, String field) {
        return NO_VALUE.equalsIgnoreCase((String) caseData.get(field));
    }

    private static boolean isAdjourned(Map<String, Object> caseData, String field) {
        return JudgeDecisions.ADJOURN_VALUE.equalsIgnoreCase((String) caseData.get(field));
    }
}
