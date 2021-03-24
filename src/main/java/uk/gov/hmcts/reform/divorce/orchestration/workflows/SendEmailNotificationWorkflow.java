package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerUpdateNotificationsEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendPetitionerNoticeOfProceedingsEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendRespondentNoticeOfProceedingsEmailTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.EventHelper.isIssueAosEvent;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentSolicitorDigital;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendEmailNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerUpdateNotificationsEmailTask sendPetitionerUpdateNotificationsEmailTask;
    private final SendPetitionerNoticeOfProceedingsEmailTask sendPetitionerNoticeOfProceedingsEmailTask;
    private final SendRespondentNoticeOfProceedingsEmailTask sendRespondentNoticeOfProceedingsEmailTask;

    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(String eventId, CaseDetails caseDetails) throws WorkflowException {
        final String caseId = caseDetails.getCaseId();
        final Map<String, Object> caseData = caseDetails.getCaseData();

        log.info(
            "CaseId: {} send petitioner email task is going to be executed for event {}",
            caseId,
            eventId
        );

        return this.execute(
            getTasks(caseId, eventId, caseData),
            caseData,
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, eventId),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(String caseId, String eventId, Map<String, Object> caseData) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        if (isRepresentedRespondentJourneyEnabled() && isIssueAosEvent(eventId) && isRespondentSolicitorDigital(caseData)) {
            log.info("CaseId: {} adding sendRespondentNoticeOfProceedingsEmailTask", caseId);
            tasks.add(sendRespondentNoticeOfProceedingsEmailTask);
        }

        if (isSolicitorDnRejectedEnabled() && isIssueAosEvent(eventId)) {
            log.info("CaseId: {} adding sendNoticeOfProceedingsEmailTask", caseId);
            tasks.add(sendPetitionerNoticeOfProceedingsEmailTask);
            return tasks.toArray(new Task[] {});
        }

        log.info("CaseId: {} adding sendPetitionerUpdateNotificationsEmailTask", caseId);
        tasks.add(sendPetitionerUpdateNotificationsEmailTask);
        return tasks.toArray(new Task[] {});
    }

    private boolean isSolicitorDnRejectedEnabled() {
        return featureToggleService.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND);
    }

    private boolean isRepresentedRespondentJourneyEnabled() {
        return featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY);
    }
}
