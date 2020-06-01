package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_PREVIOUS_STATE;

@RequiredArgsConstructor
@Component
public class WelshSetPreviousStateTask implements Task<Map<String, Object>> {
    private final CaseMaintenanceClient caseMaintenanceClient;

    @Value("#{'${ignore.states}'.split(',')}")
    private Set<String> ignoreStates;

    @Override
    public Map<String, Object> execute(TaskContext context, final Map<String, Object> payload) throws TaskException {

        String previousState = caseMaintenanceClient.retrievePetitionById(
            context.<String>getTransientObject(AUTH_TOKEN_JSON_KEY),
            context.<String>getTransientObject(CASE_ID_JSON_KEY))
            .getState();

        if (!ignoreStates.contains(previousState)) {
            payload.put(WELSH_PREVIOUS_STATE, previousState);
        }
        return payload;
    }
}
