package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerUpdateNotificationsEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendPetitionerNoticeOfProceedingsEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendRespondentNoticeOfProceedingsEmailTask;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.ISSUE_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.ISSUE_AOS_FROM_REISSUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_DIGITAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.buildOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class SendEmailNotificationWorkflowTest {

    @Mock
    private SendPetitionerUpdateNotificationsEmailTask sendPetitionerUpdateNotificationsEmailTask;

    @Mock
    private SendPetitionerNoticeOfProceedingsEmailTask sendPetitionerNoticeOfProceedingsEmailTask;

    @Mock
    private SendRespondentNoticeOfProceedingsEmailTask sendRespondentNoticeOfProceedingsEmailTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private SendEmailNotificationWorkflow sendEmailNotificationWorkflow;

    @Test
    public void executeSendNoticeOfProceedingsEmailTaskWhenIssueAosEvent() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND)).thenReturn(true);
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);
        runTestForEventExpectTaskToBeCalled(
            buildCaseData(),
            ISSUE_AOS,
            sendRespondentNoticeOfProceedingsEmailTask,
            sendPetitionerNoticeOfProceedingsEmailTask
        );
        verifyTasksWereNeverCalled(sendPetitionerUpdateNotificationsEmailTask);
    }

    @Test
    public void executeSendNoticeOfProceedingsEmailTaskWhenIssueAosFromReissueEvent() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND)).thenReturn(true);
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);
        runTestForEventExpectTaskToBeCalled(
            buildCaseData(),
            ISSUE_AOS_FROM_REISSUE,
            sendRespondentNoticeOfProceedingsEmailTask,
            sendPetitionerNoticeOfProceedingsEmailTask
        );
        verifyTasksWereNeverCalled(sendPetitionerUpdateNotificationsEmailTask);
    }

    @Test
    public void executeSendPetitionerUpdateNotificationsEmailTask() throws Exception {
        runTestForEventExpectTaskToBeCalled(buildCaseData(),"any-event", sendPetitionerUpdateNotificationsEmailTask);
        verifyTasksWereNeverCalled(sendPetitionerNoticeOfProceedingsEmailTask, sendRespondentNoticeOfProceedingsEmailTask);
    }

    @Test
    public void executeSendNoticeOfProceedingsEmailTaskWhenFeatureToggleOff() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND)).thenReturn(false);
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);
        runTestForEventExpectTaskToBeCalled(
            buildCaseData(),
            ISSUE_AOS_FROM_REISSUE,
            sendPetitionerUpdateNotificationsEmailTask
        );
        verifyTasksWereNeverCalled(sendPetitionerNoticeOfProceedingsEmailTask, sendRespondentNoticeOfProceedingsEmailTask);
    }

    @Test
    public void notExecuteSendRespondentNoticeOfProceedingsEmailTaskWhenRespondentSolicitorIsNotDigital() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND)).thenReturn(true);
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);
        runTestForEventExpectTaskToBeCalled(
            emptyMap(),
            ISSUE_AOS,
            sendPetitionerNoticeOfProceedingsEmailTask
        );
        verifyTasksWereNeverCalled(sendPetitionerUpdateNotificationsEmailTask, sendRespondentNoticeOfProceedingsEmailTask);
    }

    private void runTestForEventExpectTaskToBeCalled(Map<String, Object> testData, String eventId, Task<Map<String, Object>>... task)
        throws TaskException, WorkflowException {
        mockTasksExecution(testData, task);

        Map<String, Object> returnedCaseData = sendEmailNotificationWorkflow
            .run(buildCcdCallbackRequest(testData, eventId).getEventId(), buildCcdCallbackRequest(testData, eventId).getCaseDetails());

        assertThat(returnedCaseData, is(testData));

        verifyTasksCalledInOrder(testData, task);
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESPONDENT_SOLICITOR_DIGITAL, YES_VALUE);
        caseData.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());

        return caseData;
    }

    public static CcdCallbackRequest buildCcdCallbackRequest(Map<String, Object> caseData) {
        return buildCcdCallbackRequest(caseData, TEST_EVENT_ID);
    }

    public static CcdCallbackRequest buildCcdCallbackRequest(Map<String, Object> caseData, String eventId) {
        return CcdCallbackRequest.builder()
            .eventId(eventId)
            .caseDetails(
                CaseDetails.builder()
                    .caseId(TEST_CASE_ID)
                    .state(TEST_STATE)
                    .caseData(caseData)
                    .build()
            ).build();
    }
}
