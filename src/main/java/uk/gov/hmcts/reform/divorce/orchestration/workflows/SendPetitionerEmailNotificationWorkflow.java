package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerUpdateNotificationsEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendNoticeOfProceedingsEmailTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@RequiredArgsConstructor
public class SendPetitionerEmailNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerUpdateNotificationsEmailTask sendPetitionerUpdateNotificationsEmailTask;
    private final SendNoticeOfProceedingsEmailTask sendNoticeOfProceedingsEmailTask;

    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return this.execute(
            getTasks(ccdCallbackRequest.getEventId()),
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, ccdCallbackRequest.getEventId()),
            ImmutablePair.of(CASE_ID_JSON_KEY, ccdCallbackRequest.getCaseDetails().getCaseId())
        );
    }

    private Task<Map<String, Object>>[] getTasks(String eventId) {
        if (isSolicitorDnRejectedEnabled() && SendNoticeOfProceedingsEmailTask.isEventSupported(eventId)) {
            return new Task[] {
                sendNoticeOfProceedingsEmailTask
            };
        }

        return new Task[] {
            sendPetitionerUpdateNotificationsEmailTask
        };
    }

    private boolean isSolicitorDnRejectedEnabled() {
        return featureToggleService.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND);
    }
}
