package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.config.WelshStateTransitionConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;

@RequiredArgsConstructor
@Component
@Slf4j
public class WelshContinueInterceptTask implements Task<Map<String, Object>> {

    @Autowired
    private WelshStateTransitionConfig welshStateTransitionConfig;

    @Override
    public Map<String, Object> execute(TaskContext context, final Map<String, Object> payload) throws TaskException {
        final CaseDetails currentCaseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        final String currentState = currentCaseDetails.getState();

        log.debug("WelshContinueInterceptTask Current State {} ", currentState);
        payload.put(STATE_CCD_FIELD, welshStateTransitionConfig.getWelshStopState().get(currentState));

        return payload;
    }
}