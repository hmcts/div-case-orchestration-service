package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_AMENDED_PETITION_DRAFT_KEY;

@Component
public class CreateAmendPetitionDraftForRefusalFromCaseIdTask implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public CreateAmendPetitionDraftForRefusalFromCaseIdTask(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }


    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> draft) {
        final Map<String, Object> amendDraft = caseMaintenanceClient
            .amendPetitionForRefusalFromCaseId(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString(),
                context.getTransientObject(CASE_ID_JSON_KEY));

        context.setTransientObject(NEW_AMENDED_PETITION_DRAFT_KEY, amendDraft);
        return amendDraft;
    }
}