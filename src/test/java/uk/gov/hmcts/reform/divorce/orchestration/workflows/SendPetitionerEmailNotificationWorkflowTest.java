package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
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

@RunWith(MockitoJUnitRunner.class)
public class SendPetitionerEmailNotificationWorkflowTest {

    @Mock
    private SendPetitionerUpdateNotificationsEmailTask sendPetitionerUpdateNotificationsEmailTask;

    @Mock
    private SendNoticeOfProceedingsEmailTask sendNoticeOfProceedingsEmailTask;

    @InjectMocks
    private SendPetitionerEmailNotificationWorkflow sendPetitionerEmailNotificationWorkflow;

    @Test
    public void executeCallsAllTasksInOrder() throws Exception {
        Map<String, Object> testData = Collections.emptyMap();

        mockTasksExecution(
            testData,
            sendPetitionerUpdateNotificationsEmailTask,
            sendNoticeOfProceedingsEmailTask
        );

        Map<String, Object> returnedCaseData = sendPetitionerEmailNotificationWorkflow
            .run(buildCcdCallbackRequest(testData));

        assertThat(returnedCaseData, is(testData));

        verifyTasksCalledInOrder(
            testData,
            sendPetitionerUpdateNotificationsEmailTask,
            sendNoticeOfProceedingsEmailTask
        );
    }

    public static CcdCallbackRequest buildCcdCallbackRequest(Map<String, Object> caseData) {
        return CcdCallbackRequest.builder()
            .eventId(TEST_EVENT_ID)
            .caseDetails(
                CaseDetails.builder()
                    .caseId(TEST_CASE_ID)
                    .state(TEST_STATE)
                    .caseData(caseData)
                    .build()
            ).build();
    }
}
