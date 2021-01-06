package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.EmailNotificationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SaveToDraftStore;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_SESSION_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SEND_EMAIL;

@Component
public class SaveDraftWorkflow extends DefaultWorkflow<Map<String, Object>> {


    private final SaveToDraftStore saveToDraftStore;
    private final EmailNotificationTask emailNotificationTask;

    @Autowired
    public SaveDraftWorkflow(SaveToDraftStore saveToDraftStore,
                             EmailNotificationTask emailNotificationTask) {
        this.saveToDraftStore = saveToDraftStore;
        this.emailNotificationTask = emailNotificationTask;
    }

    public Map<String, Object> run(Map<String, Object> payLoad,
                                   String authToken,
                                   String sendEmail) throws WorkflowException {
        return this.execute(
                new Task[]{
                    saveToDraftStore,
                    emailNotificationTask
                },
                payLoad,
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(NOTIFICATION_SEND_EMAIL, sendEmail),
                ImmutablePair.of(NOTIFICATION_EMAIL, payLoad.get(DIVORCE_SESSION_PETITIONER_EMAIL))
        );
    }
}