package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JudgeDecisionHelper {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class JudgeFields {
        public static final String JUDGE_COSTS_CLAIM_GRANTED = "JudgeCostsClaimGranted";
    }

    public static class JudgeDecisions {
        public static final String YES_VALUE = "Y";
        public static final String NO_VALUE = "No";
        public static final String ADJOURN_VALUE = "Adjourn";
    }

    public static boolean isJudgeCostClaimGranted(Map<String, Object> caseData) {
        return isGranted(caseData, JudgeFields.JUDGE_COSTS_CLAIM_GRANTED);
    }

    public static boolean isJudgeCostClaimRejected(Map<String, Object> caseData) {
        return isRejected(caseData, JudgeFields.JUDGE_COSTS_CLAIM_GRANTED);
    }

    public static boolean isJudgeCostClaimAdjourned(Map<String, Object> caseData) {
        return isAdjourned(caseData, JudgeFields.JUDGE_COSTS_CLAIM_GRANTED);
    }

    private static boolean isGranted(Map<String, Object> caseData, String field) {
        return JudgeDecisions.YES_VALUE.equalsIgnoreCase((String) caseData.get(field));
    }

    private static boolean isRejected(Map<String, Object> caseData, String field) {
        return JudgeDecisions.NO_VALUE.equalsIgnoreCase((String) caseData.get(field));
    }

    private static boolean isAdjourned(Map<String, Object> caseData, String field) {
        return JudgeDecisions.ADJOURN_VALUE.equalsIgnoreCase((String) caseData.get(field));
    }
}
