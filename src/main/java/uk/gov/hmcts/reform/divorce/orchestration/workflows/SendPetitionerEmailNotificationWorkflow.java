package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerUpdateNotificationsEmail;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class SendPetitionerEmailNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerUpdateNotificationsEmail sendPetitionerUpdateNotificationsEmail;

    @Autowired
    public SendPetitionerEmailNotificationWorkflow(
            SendPetitionerUpdateNotificationsEmail sendPetitionerUpdateNotificationsEmail) {
        this.sendPetitionerUpdateNotificationsEmail = sendPetitionerUpdateNotificationsEmail;
    }

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return this.execute(
                new Task[] {
                    sendPetitionerUpdateNotificationsEmail,
                },
                ccdCallbackRequest.getCaseDetails().getCaseData(),
                ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, ccdCallbackRequest.getEventId()),
                ImmutablePair.of(CASE_ID_JSON_KEY, ccdCallbackRequest.getCaseDetails().getCaseId())
        );
    }
}
