package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerGenericUpdateNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentGenericUpdateNotificationEmail;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class SendDnPronouncedNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    SendPetitionerGenericUpdateNotificationEmail sendPetitionerGenericUpdateNotificationEmail;

    @Autowired
    SendRespondentGenericUpdateNotificationEmail sendRespondentGenericUpdateNotificationEmail;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        return this.execute(
                new Task[] {
                    sendPetitionerGenericUpdateNotificationEmail,
                    sendRespondentGenericUpdateNotificationEmail
                },
                ccdCallbackRequest.getCaseDetails().getCaseData(),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
