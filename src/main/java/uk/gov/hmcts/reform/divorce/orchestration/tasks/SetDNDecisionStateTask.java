package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features.DN_REFUSAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_ADMIN_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_ADMIN_ERROR_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@RequiredArgsConstructor
public class SetDNDecisionStateTask implements Task<Map<String, Object>> {

    private final FeatureToggleService featureToggleService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        Map<String, Object> payload = new HashMap<>(caseData);

        String newCaseEndState = AWAITING_CLARIFICATION;

        if (isDnGranted(caseData)) {
            newCaseEndState = AWAITING_PRONOUNCEMENT;

        } else {
            if (featureToggleService.isFeatureEnabled(DN_REFUSAL)) {
                String refusalDecision = (String) caseData.getOrDefault(REFUSAL_DECISION_CCD_FIELD, EMPTY_STRING);
                switch (refusalDecision) {
                    case DN_REFUSED_REJECT_OPTION:
                        newCaseEndState = DN_REFUSED;
                        break;
                    case DN_REFUSED_ADMIN_ERROR_OPTION:
                        newCaseEndState = AWAITING_ADMIN_CLARIFICATION;
                        break;
                    default:
                        newCaseEndState = AWAITING_CLARIFICATION;
                }
            }
        }
        payload.put(STATE_CCD_FIELD, newCaseEndState);
        return payload;
    }

    private boolean isDnGranted(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(DECREE_NISI_GRANTED_CCD_FIELD));
    }
}