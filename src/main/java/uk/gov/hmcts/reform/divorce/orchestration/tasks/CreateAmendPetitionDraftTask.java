package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_AMENDED_PETITION_DRAFT_KEY;

@Component
@RequiredArgsConstructor
public class CreateAmendPetitionDraftTask implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> draft) {
        try {
            final Map<String, Object> amendDraft = caseMaintenanceClient.amendPetition(context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString());

            context.setTransientObject(NEW_AMENDED_PETITION_DRAFT_KEY, amendDraft);
        } catch (FeignException exception) {
            if (exception.status() == HttpStatus.NOT_FOUND.value()) {
                String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
                throw new TaskException("AmendPetitionDraft: Case not found - please check the case ID: " + caseId, exception);
            } else if (exception.status() == HttpStatus.MULTIPLE_CHOICES.value()) {
                throw new TaskException("AmendPetitionDraft: Multiple cases found", exception);
            }
            throw exception;
        }

        // return empty as next step (update case state AmendPetition) needs no data (empty)
        return new HashMap<>();
    }
}
