package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
public class RespondentAosAnswersProcessor implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        Map<String, Object> payloadToReturn = new HashMap<>(payload);

        payloadToReturn.put(STATE_CCD_FIELD, returnNewStateForCase(payload));
        payloadToReturn.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);

        return payloadToReturn;
    }

    private String returnNewStateForCase(Map<String, Object> payload) throws TaskException {
        String newState;

        boolean respondentDefendingDivorce = Optional.ofNullable(payload.get(RESP_WILL_DEFEND_DIVORCE))
            .map(String.class::cast)
            .map(YES_VALUE::equalsIgnoreCase)
            .orElse(false);

        String reasonForDivorce = getMandatoryPropertyValueAsString(payload, D_8_REASON_FOR_DIVORCE);

        if (respondentDefendingDivorce) {
            newState = AOS_SUBMITTED_AWAITING_ANSWER;
        } else if (isReasonAdulteryOrTwoYearsSeparation(reasonForDivorce)) {
            boolean respondentAdmitsOrConsent = Optional.ofNullable(payload.get(RESP_ADMIT_OR_CONSENT_TO_FACT))
                .map(String.class::cast)
                .map(YES_VALUE::equalsIgnoreCase)
                .orElse(false);

            if (respondentAdmitsOrConsent) {
                newState = AWAITING_DECREE_NISI;
            } else {
                newState = AOS_COMPLETED;
            }
        } else {
            newState = AWAITING_DECREE_NISI;
        }

        return newState;
    }

    private boolean isReasonAdulteryOrTwoYearsSeparation(String reasonForDivorce) {
        return ADULTERY.equalsIgnoreCase(reasonForDivorce) || SEPARATION_TWO_YEARS.equalsIgnoreCase(reasonForDivorce);
    }

}