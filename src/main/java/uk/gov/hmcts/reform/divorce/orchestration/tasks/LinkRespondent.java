package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;

@Component
public class LinkRespondent implements Task<UserDetails> {
    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public LinkRespondent(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public UserDetails execute(TaskContext context, UserDetails payLoad) {
        boolean isCoRespondent = (boolean) context.getTransientObject(IS_CO_RESPONDENT);
        if (isCoRespondent) {
            caseMaintenanceClient.linkCoRespondent(
                String.valueOf(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
                String.valueOf(context.getTransientObject(CASE_ID_JSON_KEY)),
                String.valueOf(context.getTransientObject(RESPONDENT_LETTER_HOLDER_ID))
            );
        } else {
            caseMaintenanceClient.linkRespondent(
                String.valueOf(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
                String.valueOf(context.getTransientObject(CASE_ID_JSON_KEY)),
                String.valueOf(context.getTransientObject(RESPONDENT_LETTER_HOLDER_ID))
            );
        }

        return payLoad;
    }
}
