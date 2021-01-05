package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraftTask;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)

public class DeleteDraftTaskWorkflowTest {

    @Mock
    private DeleteDraftTask deleteDraftTask;

    @InjectMocks
    private DeleteDraftWorkflow target;

    @Test
    public void givenADraft_whenExecuteDeleteDraftWorkflow_thenDeleteTaskIsExecuted() throws WorkflowException {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> draftPayload = mock(Map.class);
        ArgumentMatcher<TaskContext> contextWithAuthTokenMatcher =
            argument -> argument.getTransientObject(AUTH_TOKEN_JSON_KEY) != null;

        when(deleteDraftTask.execute(argThat(contextWithAuthTokenMatcher),
                eq(payload))).thenReturn(draftPayload);

        assertEquals(draftPayload, target.run(AUTH_TOKEN));

        verify(deleteDraftTask).execute(argThat(contextWithAuthTokenMatcher), eq(payload));
    }
}
