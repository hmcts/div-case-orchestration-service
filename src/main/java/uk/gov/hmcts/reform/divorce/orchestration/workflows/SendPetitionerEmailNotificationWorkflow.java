package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SendPetitionerEmailNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerUpdateNotificationsEmailTask sendPetitionerUpdateNotificationsEmailTask;
    private final SendNoticeOfProceedingsEmailTask sendNoticeOfProceedingsEmailTask;

    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        final String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        final String eventId = ccdCallbackRequest.getEventId();

        log.info(
            "CaseId: {} send petitioner email task is going to be executed for event {}",
            caseId,
            eventId
        );

        return this.execute(
            getTasks(caseId, eventId),
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, eventId),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(String caseId, String eventId) {
        if (isSolicitorDnRejectedEnabled() && SendNoticeOfProceedingsEmailTask.isEventSupported(eventId)) {
            log.info("CaseId: {} adding sendNoticeOfProceedingsEmailTask", caseId);
            return new Task[] {
                sendNoticeOfProceedingsEmailTask
            };
        }

        log.info("CaseId: {} adding sendPetitionerUpdateNotificationsEmailTask", caseId);

        return new Task[] {
            sendPetitionerUpdateNotificationsEmailTask
        };
    }

    private boolean isSolicitorDnRejectedEnabled() {
        return featureToggleService.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND);
    }
}
