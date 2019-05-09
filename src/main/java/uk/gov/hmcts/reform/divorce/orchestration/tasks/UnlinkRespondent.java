package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class UnlinkRespondent implements Task<UserDetails> {
    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public UnlinkRespondent(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public UserDetails execute(TaskContext context, UserDetails payLoad) {
        caseMaintenanceClient.unlinkRespondent(
            context.getTransientObject(AUTH_TOKEN_JSON_KEY),
            context.getTransientObject(CASE_ID_JSON_KEY)
        );

        return payLoad;
    }
}
