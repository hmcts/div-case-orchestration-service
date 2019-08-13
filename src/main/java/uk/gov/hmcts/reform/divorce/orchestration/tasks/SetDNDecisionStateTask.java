package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features.DN_REFUSAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@RequiredArgsConstructor
public class SetDNDecisionStateTask implements Task<Map<String, Object>> {

    public static final String DN_REFUSED_REJECT_OPTION = "reject";

    private final FeatureToggleService featureToggleService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        Map<String, Object> payload = new HashMap<>(caseData);

        String newCaseEndState = AWAITING_CLARIFICATION;

        if (isDnGranted(caseData)) {
            newCaseEndState = AWAITING_PRONOUNCEMENT;

        } else {
            if (featureToggleService.isFeatureEnabled(DN_REFUSAL)) {
                if (isDnRejected(caseData)) {
                    newCaseEndState = DN_REFUSED;
                }
            }
        }
        payload.put(STATE_CCD_FIELD, newCaseEndState);
        return payload;
    }

    private boolean isDnRejected(Map<String, Object> caseData) {
        return DN_REFUSED_REJECT_OPTION.equalsIgnoreCase((String) caseData.get(REFUSAL_DECISION_CCD_FIELD));
    }

    private boolean isDnGranted(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(DECREE_NISI_GRANTED_CCD_FIELD));
    }
}