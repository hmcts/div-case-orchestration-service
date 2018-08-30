package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveDraft;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveDraftWorkflowTest {

    @Mock
    private RetrieveDraft retrieveDraft;

    @InjectMocks
    private RetrieveDraftWorkflow target;

    @Test
    public void givenADraft_whenExecuteSaveDraftWorkflow_thenExecuteAllTaskInOrder() throws WorkflowException {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> draftPayload = mock(Map.class);

        when(retrieveDraft.execute(Mockito.any(TaskContext.class),
                eq(payload), eq(AUTH_TOKEN))).thenReturn(draftPayload);

        assertEquals(draftPayload, target.run(AUTH_TOKEN));

        verify(retrieveDraft).execute(Mockito.any(TaskContext.class),eq(payload), eq(AUTH_TOKEN));
    }

}
