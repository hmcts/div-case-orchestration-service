package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.EmailNotification;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SaveToDraftStore;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class SaveDraftWorkflowTest {


    @Mock
    private SaveToDraftStore saveToDraftStore;
    @Mock
    private EmailNotification emailNotification;
    @InjectMocks
    private SaveDraftWorkflow target;


    @Test
    public void givenADraft_whenExecuteSaveDraftWorkflow_thenExecuteAllTaskInOrder() throws WorkflowException {
        Map<String, Object> payload = mock(Map.class);
        Map<String, Object> draftSavedPayload = mock(Map.class);
        Map<String, Object> emailNotificationPayload = mock(Map.class);
        final boolean divorceFormat = true;

        when(saveToDraftStore.execute(Mockito.any(TaskContext.class), eq(payload), eq(AUTH_TOKEN),
                eq(TEST_USER_EMAIL),eq(divorceFormat)))
                .thenReturn(draftSavedPayload);
        when(emailNotification.execute(Mockito.any(TaskContext.class), eq(draftSavedPayload),
                eq(AUTH_TOKEN), eq(TEST_USER_EMAIL), eq(divorceFormat))).thenReturn(emailNotificationPayload);

        assertEquals(emailNotificationPayload, target.run(payload, AUTH_TOKEN, TEST_USER_EMAIL, divorceFormat));

        verify(saveToDraftStore).execute(Mockito.any(TaskContext.class),
                eq(payload), eq(AUTH_TOKEN), eq(TEST_USER_EMAIL), eq(divorceFormat));
        verify(emailNotification).execute(Mockito.any(TaskContext.class), eq(draftSavedPayload),
                eq(AUTH_TOKEN), eq(TEST_USER_EMAIL),eq(divorceFormat));
    }

    @Test
    public void givenAError_whenExecuteSaveDraftWorkflow_thenStopExecution() throws WorkflowException {
        final Map<String, Object> payload = mock(Map.class);
        final Map<String, Object> draftSavedPayload = mock(Map.class);
        final boolean divorceFormat = true;

        when(saveToDraftStore.execute(Mockito.any(TaskContext.class), eq(payload), eq(AUTH_TOKEN),
                eq(TEST_USER_EMAIL), eq(divorceFormat)))
                .then(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        TaskContext context = invocation.getArgument(0);
                        context.setTaskFailed(true);
                        return draftSavedPayload;
                    }
                });
        target.run(payload, AUTH_TOKEN, TEST_USER_EMAIL, divorceFormat);

        verify(saveToDraftStore).execute(Mockito.any(TaskContext.class), eq(payload),
                eq(AUTH_TOKEN), eq(TEST_USER_EMAIL), eq(divorceFormat));
        verify(emailNotification, never()).execute(Mockito.any(TaskContext.class), any(), any(), any(),any());
    }
}
