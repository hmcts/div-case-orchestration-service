package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_AMENDED_PETITION_DRAFT_KEY;

@Component
@RequiredArgsConstructor
public class CreateAmendPetitionDraftTask implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> draft) {
        final Map<String, Object> amendDraft = caseMaintenanceClient
            .amendPetition(context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString());

        context.setTransientObject(NEW_AMENDED_PETITION_DRAFT_KEY, amendDraft);

        // return empty as next step (update case state AmendPetition) needs no data (empty)
        return new HashMap<>();
    }
}
