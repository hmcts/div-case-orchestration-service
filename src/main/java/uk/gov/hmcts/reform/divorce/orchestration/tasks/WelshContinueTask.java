package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;

@RequiredArgsConstructor
@Component
public class WelshContinueTask implements Task<Map<String, Object>> {
    private final CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context, final Map<String, Object> payload) throws TaskException {
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String caseIDJsonKey = context.getTransientObject(CASE_ID_JSON_KEY);

        Optional<String> nextEvent = Optional.ofNullable(payload.get(WELSH_NEXT_EVENT)).map(String.class::cast);

        if (nextEvent.isPresent()) {
            try {
                payload.remove(WELSH_NEXT_EVENT);
                caseMaintenanceClient.updateCase(
                        authToken,
                        caseIDJsonKey,
                        nextEvent.get(),
                        payload
                );
            } catch (FeignException exception) {
                payload.put(WELSH_NEXT_EVENT, nextEvent.get());
                throw new TaskException(String.join(" ", "For case:", caseIDJsonKey, "update failed"), exception);
            }
        }
        return payload;
    }
}
