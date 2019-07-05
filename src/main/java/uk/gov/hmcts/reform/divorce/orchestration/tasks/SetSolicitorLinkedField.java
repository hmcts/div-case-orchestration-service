package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINK_RESPONDENT_GENERIC_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_LINKED_EMAIL;

@AllArgsConstructor
@Component
public class SetSolicitorLinkedField implements Task<UserDetails> {

    @Autowired
    private final CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public UserDetails execute(TaskContext context, UserDetails payload) throws TaskException {

        Map<String, Object> updateFields = new HashMap<>();
        try {
            updateFields.put(SOLICITOR_LINKED_EMAIL, context.getTransientObject(SOLICITOR_LINKED_EMAIL));

            caseMaintenanceClient.updateCase(
                    context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                    context.getTransientObject(CASE_ID_JSON_KEY),
                    LINK_RESPONDENT_GENERIC_EVENT_ID,
                    updateFields
            );
        } catch (FeignException exception) {
            throw new TaskException("Case update failed", exception);
        }

        return payload;
    }
}
