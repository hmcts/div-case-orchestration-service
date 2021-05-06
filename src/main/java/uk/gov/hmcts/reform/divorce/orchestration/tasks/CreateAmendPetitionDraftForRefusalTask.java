package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CreateAmendPetitionDraftForRefusalTask implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public CreateAmendPetitionDraftForRefusalTask(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }


    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> draft) {
        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
        try{
            final Map<String, Object> amendDraft = caseMaintenanceClient
                .amendPetitionForRefusal(context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString());

            context.setTransientObject(NEW_AMENDED_PETITION_DRAFT_KEY, amendDraft);
        } catch (FeignException exception) {
            if (exception.status() == HttpStatus.NOT_FOUND.value()) {
                throw new TaskException("amendPetition for DnRejection: Case not found - please check the case ID: " + caseId, exception);
            } else if (exception.status() == HttpStatus.MULTIPLE_CHOICES.value()) {
                throw new TaskException("amendPetition for DnRejection: Multiple cases found", exception);
            } else {
                throw new TaskException("amendPetition for DnRejection: An error occurred", exception);
            }
        }
        // return empty as next step (update case state AmendPetition) needs no data (empty)
        return new HashMap<>();
    }
}