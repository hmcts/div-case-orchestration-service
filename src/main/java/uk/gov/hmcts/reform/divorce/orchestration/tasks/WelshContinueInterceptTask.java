package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_TRANSLATION_REQUESTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_RESPONSE_AWAITING_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PENDING_REJECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUBMITTED;

@RequiredArgsConstructor
@Component
@Slf4j
public class WelshContinueInterceptTask implements Task<Map<String, Object>> {
    @Override
    public Map<String, Object> execute(TaskContext context, final Map<String, Object> payload) throws TaskException {
        final CaseDetails currentCaseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        final String currentState = currentCaseDetails.getState();

        log.debug("WelshContinueInterceptTask Current State {} ", currentState);
        switch (currentState) {
            case BO_TRANSLATION_REQUESTED:
                payload.put(STATE_CCD_FIELD, SUBMITTED);
                break;
            case SUBMITTED:
            case PENDING_REJECTION:
                payload.put(STATE_CCD_FIELD, BO_TRANSLATION_REQUESTED);
                break;
            case AOS_STARTED:
            case AOS_OVERDUE:
                payload.put(STATE_CCD_FIELD, BO_WELSH_RESPONSE_AWAITING_REVIEW);
                break;
            default:
        }
        return payload;
    }
}
