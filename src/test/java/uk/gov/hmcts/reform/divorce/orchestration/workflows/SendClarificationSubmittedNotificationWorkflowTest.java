package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.PetitionerClarificationSubmittedNotificationEmailTask;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;

@RunWith(MockitoJUnitRunner.class)
public class SendClarificationSubmittedNotificationWorkflowTest {

    @Mock
    private PetitionerClarificationSubmittedNotificationEmailTask petitionerClarificationSubmittedNotificationEmailTask;

    @InjectMocks
    private SendClarificationSubmittedNotificationWorkflow sendClarificationSubmittedNotificationWorkflow;

    @Test
    public void shouldCallPetitionerClarificationSubmittedNotificationEmailTask() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();

        mockTasksExecution(
            caseData,
            petitionerClarificationSubmittedNotificationEmailTask
        );

        sendClarificationSubmittedNotificationWorkflow.run(
            CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder().caseData(caseData).build())
                .build()
        );

        verifyTaskWasCalled(caseData, petitionerClarificationSubmittedNotificationEmailTask);
    }
}
