package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerUpdateNotificationsEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendNoticeOfProceedingsEmailTask;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class SendPetitionerEmailNotificationWorkflowTest {

    @Mock
    private SendPetitionerUpdateNotificationsEmailTask sendPetitionerUpdateNotificationsEmailTask;

    @Mock
    private SendNoticeOfProceedingsEmailTask sendNoticeOfProceedingsEmailTask;

    @InjectMocks
    private SendPetitionerEmailNotificationWorkflow sendPetitionerEmailNotificationWorkflow;

    @Test
    public void executeSendNoticeOfProceedingsEmailTaskWhenIssueAosEvent() throws Exception {
        runTestForEventExpectTaskToBeCalled(
            SendNoticeOfProceedingsEmailTask.EVENT_ISSUE_AOS,
            sendNoticeOfProceedingsEmailTask
        );
        verifyTasksWereNeverCalled(sendPetitionerUpdateNotificationsEmailTask);
    }

    @Test
    public void executeSendNoticeOfProceedingsEmailTaskWhenIssueAosFromReissueEvent() throws Exception {
        runTestForEventExpectTaskToBeCalled(
            SendNoticeOfProceedingsEmailTask.EVENT_ISSUE_AOS_FROM_REISSUE,
            sendNoticeOfProceedingsEmailTask
        );
        verifyTasksWereNeverCalled(sendPetitionerUpdateNotificationsEmailTask);
    }


    @Test
    public void executeSendPetitionerUpdateNotificationsEmailTask() throws Exception {
        runTestForEventExpectTaskToBeCalled("any-event", sendPetitionerUpdateNotificationsEmailTask);
        verifyTasksWereNeverCalled(sendNoticeOfProceedingsEmailTask);
    }

    private void runTestForEventExpectTaskToBeCalled(String eventId, Task<Map<String, Object>> task) throws TaskException, WorkflowException {
        Map<String, Object> testData = Collections.emptyMap();

        mockTasksExecution(testData, task);

        Map<String, Object> returnedCaseData = sendPetitionerEmailNotificationWorkflow
            .run(buildCcdCallbackRequest(testData, eventId));

        assertThat(returnedCaseData, is(testData));

        verifyTasksCalledInOrder(testData, task);
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
