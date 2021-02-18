package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerSubmissionNotificationEmailTask;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.SendEmailNotificationWorkflowTest.buildCcdCallbackRequest;

@RunWith(MockitoJUnitRunner.class)
public class SendPetitionerSubmissionNotificationWorkflowTest {

    @Mock
    private SendPetitionerSubmissionNotificationEmailTask sendPetitionerSubmissionNotificationEmailTask;

    @InjectMocks
    private SendPetitionerSubmissionNotificationWorkflow sendPetitionerSubmissionNotificationWorkflow;

    @Test
    public void executeCallsAllTasksInOrder() throws Exception {
        Map<String, Object> testData = Collections.emptyMap();

        mockTasksExecution(
            testData,
            sendPetitionerSubmissionNotificationEmailTask
        );

        Map<String, Object> returnedCaseData = sendPetitionerSubmissionNotificationWorkflow
            .run(buildCcdCallbackRequest(testData));

        assertThat(returnedCaseData, is(testData));

        verifyTasksCalledInOrder(
            testData,
            sendPetitionerSubmissionNotificationEmailTask
        );
    }
}
