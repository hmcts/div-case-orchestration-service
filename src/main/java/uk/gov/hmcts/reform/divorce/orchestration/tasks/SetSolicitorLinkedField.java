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
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINK_RESPONDENT_GENERIC_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_RESPONDENT_DATA_ERROR_KEY;

@AllArgsConstructor
@Component
public class SetSolicitorLinkedField implements Task<UserDetails> {

    private static final String SOLICITOR_LINKED_EMAIL = "RespSolicitorLinkedEmail";

    @Autowired
    private final AuthUtil authUtil;

    @Autowired
    private final CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public UserDetails execute(TaskContext context, UserDetails payload) throws TaskException {

        Map<String, Object> updateFields = new HashMap<>();
        try {
            updateFields.put(SOLICITOR_LINKED_EMAIL, payload.getEmail());

            final String caseWorkerToken = authUtil.getCaseworkerToken();

            caseMaintenanceClient.updateCase(
                    caseWorkerToken,
                    context.getTransientObject(CASE_ID_JSON_KEY),
                    LINK_RESPONDENT_GENERIC_EVENT_ID,
                    updateFields
            );
        } catch (FeignException exception) {
            context.setTransientObject(UPDATE_RESPONDENT_DATA_ERROR_KEY, payload);
            throw new TaskException("Case update failed", exception);
        }

        return payload;
    }
}
