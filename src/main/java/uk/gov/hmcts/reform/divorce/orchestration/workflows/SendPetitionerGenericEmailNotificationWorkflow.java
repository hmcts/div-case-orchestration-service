package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerGenericUpdateNotificationEmail;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class SendPetitionerGenericEmailNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerGenericUpdateNotificationEmail sendPetitionerGenericUpdateNotificationEmail;

    @Autowired
    public SendPetitionerGenericEmailNotificationWorkflow(
            SendPetitionerGenericUpdateNotificationEmail sendPetitionerGenericUpdateNotificationEmail) {
        this.sendPetitionerGenericUpdateNotificationEmail = sendPetitionerGenericUpdateNotificationEmail;
    }

    public Map<String, Object> run(CreateEvent caseRequestDetails) throws WorkflowException {
        return this.execute(
                new Task[] {
                    sendPetitionerGenericUpdateNotificationEmail,
                },
                caseRequestDetails.getCaseDetails().getCaseData(),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseRequestDetails.getCaseDetails().getCaseId())
        );
    }
}