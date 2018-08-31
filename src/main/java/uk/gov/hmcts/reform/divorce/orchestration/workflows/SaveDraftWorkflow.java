package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.EmailNotification;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SaveToDraftStore;

import java.util.Map;

@Component
public class SaveDraftWorkflow extends DefaultWorkflow<Map<String, Object>> {


    private final SaveToDraftStore saveToDraftStore;
    private final EmailNotification emailNotification;

    @Autowired
    public SaveDraftWorkflow(SaveToDraftStore saveToDraftStore,
                             EmailNotification emailNotification) {
        this.saveToDraftStore = saveToDraftStore;
        this.emailNotification = emailNotification;
    }

    public Map<String, Object> run(Map<String, Object> payLoad,
                                   String authToken,
                                   String notificationEmail) throws WorkflowException {
        return this.execute(
                new Task[]{
                    saveToDraftStore,
                    emailNotification
                },
                payLoad,
                authToken,
                notificationEmail);
    }
}