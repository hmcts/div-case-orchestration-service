package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_RESPONSE_AWAITING_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_NEXT_WELSH_CASE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_PREVIOUS_STATE;

@RequiredArgsConstructor
@Component
public class WelshSetPreviousStateTask implements Task<Map<String, Object>> {
    private final CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context, final Map<String, Object> payload) throws TaskException {
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String caseIDJsonKey = context.getTransientObject(CASE_ID_JSON_KEY);

        String previousState = caseMaintenanceClient.retrievePetitionById(
            context.<String>getTransientObject(AUTH_TOKEN_JSON_KEY),
            context.<String>getTransientObject(CASE_ID_JSON_KEY))
            .getState();;

        if(!BO_WELSH_RESPONSE_AWAITING_REVIEW.equals(previousState)) {
            payload.put(WELSH_PREVIOUS_STATE, previousState);
            ;
            try {
                caseMaintenanceClient.updateCase(
                    authToken,
                    caseIDJsonKey,
                    UPDATE_NEXT_WELSH_CASE_EVENT,
                    payload
                );
            } catch (FeignException exception) {
                payload.put(WELSH_PREVIOUS_STATE, previousState);
                throw new TaskException(String.join(" ", "For case:", caseIDJsonKey, "update failed for event id", UPDATE_NEXT_WELSH_CASE_EVENT), exception);
            }
        }
        return payload;
    }
}
