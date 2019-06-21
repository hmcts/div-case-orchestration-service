package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_RESPONDENT_DATA_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SetSolicitorLinkedField implements Task<UserDetails> {

    private static final String SOLICITOR_LINKED_TO_CASE = "RespSolicitorLinked";

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public UserDetails execute(TaskContext context, UserDetails payload) throws TaskException {

        Map<String, Object> updateFields = new HashMap<>();
        try {
            CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

            updateFields.put(SOLICITOR_LINKED_TO_CASE, YES_VALUE);

            caseMaintenanceClient.updateCase(
                    context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                    context.getTransientObject(CASE_ID_JSON_KEY),
                    caseDetails.getState(),
                    updateFields
            );
        } catch (FeignException ex) {
            context.setTransientObject(UPDATE_RESPONDENT_DATA_ERROR_KEY, payload);
            throw new TaskException("Case update failed", ex);
        }

        return payload;
    }
}
