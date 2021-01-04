package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class DeleteDraftTaskTest {
    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private DeleteDraftTask target;

    @Test
    public void givenUserToken_whenExecuteDeleteDraftTask_thenDeleteDraftRequestIsSentToCMS() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> payload  = mock(Map.class);

        Assert.assertEquals(payload, target.execute(context, payload));

        verify(caseMaintenanceClient).deleteDraft(AUTH_TOKEN);
    }
}
