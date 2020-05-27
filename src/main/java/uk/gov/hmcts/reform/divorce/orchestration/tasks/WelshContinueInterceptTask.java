package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_PREVIOUS_STATE;

@RequiredArgsConstructor
@Component
@Slf4j
public class WelshContinueInterceptTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, final Map<String, Object> payload) throws TaskException {
        Optional.ofNullable(payload.get(WELSH_PREVIOUS_STATE)).map(String.class::cast)
            .ifPresent(nextState ->  {
                log.debug("WelshContinueInterceptTask Current State {} ", nextState);
                payload.put(STATE_CCD_FIELD, nextState);
            });

        return payload;
    }
}