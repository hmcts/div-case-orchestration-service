package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features.DN_REFUSAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_ADMIN_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_GRANT_DN_MAKE_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_ADMIN_ERROR_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_LA_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetDNDecisionStateTask implements Task<Map<String, Object>> {

    private final FeatureToggleService featureToggleService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        Map<String, Object> payload = new HashMap<>(caseData);

        String newCaseEndState = AWAITING_CLARIFICATION;

        if (isDnGranted(caseData)) {
            newCaseEndState = evaluateStop(caseData, payload, AWAITING_PRONOUNCEMENT);

        } else {
            if (featureToggleService.isFeatureEnabled(DN_REFUSAL)) {
                String refusalDecision = (String) caseData.getOrDefault(REFUSAL_DECISION_CCD_FIELD, EMPTY_STRING);
                switch (refusalDecision) {
                    case DN_REFUSED_REJECT_OPTION:
                        newCaseEndState = evaluateStop(caseData, payload, DN_REFUSED);
                        break;
                    case DN_REFUSED_ADMIN_ERROR_OPTION:
                        newCaseEndState = AWAITING_ADMIN_CLARIFICATION;
                        break;
                    default:
                        newCaseEndState = evaluateStop(caseData, payload, AWAITING_CLARIFICATION);
                }
            }
        }
        log.info("Set case state on DN decision for case Id : {} , state {}", context.getTransientObject(CASE_ID_JSON_KEY), newCaseEndState);
        payload.put(STATE_CCD_FIELD, newCaseEndState);
        return payload;
    }

    private String evaluateStop(Map<String, Object> caseData, Map<String, Object> payload, String endState) {
        if (CaseDataUtils.isRejectReasonAddInfoAwaitingTranslation(caseData)) {
            endState = WELSH_LA_DECISION;
            payload.put(WELSH_NEXT_EVENT, BO_WELSH_GRANT_DN_MAKE_DECISION);
        }
        return endState;
    }

    private boolean isDnGranted(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(DECREE_NISI_GRANTED_CCD_FIELD));
    }
}