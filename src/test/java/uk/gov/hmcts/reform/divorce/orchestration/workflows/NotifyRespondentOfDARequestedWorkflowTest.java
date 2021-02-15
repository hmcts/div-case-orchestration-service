package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaRequestedNotifyRespondentEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.decreeabsolute.DaRequestedPetitionerSolicitorEmailTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class NotifyRespondentOfDARequestedWorkflowTest {

    @Mock
    private SendDaRequestedNotifyRespondentEmailTask sendDaRequestedNotifyRespondentEmailTask;

    @Mock
    private DaRequestedPetitionerSolicitorEmailTask daRequestedPetitionerSolicitorEmailTask;

    @InjectMocks
    private NotifyRespondentOfDARequestedWorkflow notifyRespondentOfDARequestedWorkflow;

    @Test
    public void callsTheRequiredTask() throws WorkflowException, TaskException {

        final Map<String, Object> payload = new HashMap<>();
        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId(TEST_CASE_ID).build())
            .build();

        mockTasksExecution(
            payload,
            sendDaRequestedNotifyRespondentEmailTask,
            daRequestedPetitionerSolicitorEmailTask
        );

        assertThat(notifyRespondentOfDARequestedWorkflow.run(ccdCallbackRequest), is(payload));
        verifyTasksCalledInOrder(
            payload,
            sendDaRequestedNotifyRespondentEmailTask,
            daRequestedPetitionerSolicitorEmailTask
        );
    }

}
