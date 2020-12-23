package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetAmendPetitionFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendPetitionerAmendEmailTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithCaseDetails;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class SendPetitionerAmendEmailNotificationTaskWorkflowTest {

    @Mock
    private GetAmendPetitionFeeTask getAmendPetitionFeeTask;

    @Mock
    private SendPetitionerAmendEmailTask sendPetitionerAmendEmailTask;

    @InjectMocks
    private SendPetitionerAmendEmailNotificationWorkflow sendPetitionerAmendEmailNotificationWorkflow;

    @Test
    public void run() throws Exception {
        contextWithCaseDetails();

        HashMap<String, Object> caseData = new HashMap<>();

        mockTasksExecution(
            caseData,
            getAmendPetitionFeeTask,
            sendPetitionerAmendEmailTask

        );

        executeWorkflowRun(caseData);

        verifyTasksCalledInOrder(
            caseData,
            getAmendPetitionFeeTask,
            sendPetitionerAmendEmailTask
        );
    }

    private void executeWorkflowRun(HashMap<String, Object> caseData) throws WorkflowException {
        Map<String, Object> returned = sendPetitionerAmendEmailNotificationWorkflow.run(
            CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .caseData(caseData)
                    .build())
                .build()
        );

        MatcherAssert.assertThat(returned, is(caseData));
    }
}