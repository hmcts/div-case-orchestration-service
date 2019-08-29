package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerAOSOverdueNotificationEmail;

import java.util.Map;

@Component
public class SendPetitionerAOSOverdueNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private SendPetitionerAOSOverdueNotificationEmail sendPetitionerAOSOverdueNotificationEmail;

    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return this.execute(
            new Task[] {
                sendPetitionerAOSOverdueNotificationEmail
            },
            ccdCallbackRequest.getCaseDetails().getCaseData()
        );
    }

}
