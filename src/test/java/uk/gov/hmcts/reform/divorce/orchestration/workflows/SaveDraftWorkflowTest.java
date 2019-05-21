package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.EmailNotification;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SaveToDraftStore;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_SESSION_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SEND_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class SaveDraftWorkflowTest {


    @Mock
    private SaveToDraftStore saveToDraftStore;
    @Mock
    private EmailNotification emailNotification;
    @InjectMocks
    private SaveDraftWorkflow target;

    private static final ArgumentMatcher<TaskContext> CONTEXT_WITH_AUTH_TOKEN_AND_EMAIL_MATCHER =
        argument -> argument.getTransientObject(AUTH_TOKEN_JSON_KEY) != null
        && argument.getTransientObject(NOTIFICATION_SEND_EMAIL) != null
        && argument.getTransientObject(NOTIFICATION_EMAIL) != null;

    @SuppressWarnings("unchecked")
    @Test
    public void givenADraft_whenExecuteSaveDraftWorkflow_thenExecuteAllTaskInOrder() throws WorkflowException {
        Map<String, Object> payload = Collections.singletonMap(DIVORCE_SESSION_PETITIONER_EMAIL, TEST_USER_EMAIL);
        Map<String, Object> draftSavedPayload = mock(Map.class);
        Map<String, Object> emailNotificationPayload = mock(Map.class);

        when(saveToDraftStore.execute(argThat(CONTEXT_WITH_AUTH_TOKEN_AND_EMAIL_MATCHER), eq(payload)))
                .thenReturn(draftSavedPayload);
        when(emailNotification.execute(argThat(CONTEXT_WITH_AUTH_TOKEN_AND_EMAIL_MATCHER), eq(draftSavedPayload)))
                .thenReturn(emailNotificationPayload);

        assertEquals(emailNotificationPayload, target.run(payload, AUTH_TOKEN, Boolean.TRUE.toString()));

        verify(saveToDraftStore).execute(argThat(CONTEXT_WITH_AUTH_TOKEN_AND_EMAIL_MATCHER),
                eq(payload));
        verify(emailNotification).execute(argThat(CONTEXT_WITH_AUTH_TOKEN_AND_EMAIL_MATCHER), eq(draftSavedPayload));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenAError_whenExecuteSaveDraftWorkflow_thenStopExecution() throws WorkflowException {
        final Map<String, Object> payload = Collections.singletonMap(DIVORCE_SESSION_PETITIONER_EMAIL, TEST_USER_EMAIL);
        final Map<String, Object> draftSavedPayload = mock(Map.class);

        when(saveToDraftStore.execute(argThat(CONTEXT_WITH_AUTH_TOKEN_AND_EMAIL_MATCHER), eq(payload)))
                .then(invocation -> {
                    TaskContext context = invocation.getArgument(0);
                    context.setTaskFailed(true);
                    return draftSavedPayload;
                });
        target.run(payload, AUTH_TOKEN, Boolean.TRUE.toString());

        verify(saveToDraftStore).execute(argThat(CONTEXT_WITH_AUTH_TOKEN_AND_EMAIL_MATCHER), eq(payload));
        verify(emailNotification, never()).execute(any(TaskContext.class), any());
    }
}
