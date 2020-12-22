package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AuthenticateRespondentTask;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticateRespondentTaskWorkflowUTest {
    private static final String AUTH_TOKEN = "authtoken";

    @Mock
    private AuthenticateRespondentTask authenticateRespondentTask;

    @InjectMocks
    private AuthenticateRespondentWorkflow classUnderTest;

    private DefaultTaskContext defaultTaskContext;

    @Before
    public void setup() {
        defaultTaskContext = new DefaultTaskContext();
        defaultTaskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
    }

    @Test
    public void whenRun_thenProceedAsExpected() throws Exception {
        final boolean expected = true;

        Mockito.when(authenticateRespondentTask.execute(defaultTaskContext, null)).thenReturn(expected);

        classUnderTest.run(AUTH_TOKEN);

        Mockito.verify(authenticateRespondentTask).execute(defaultTaskContext, null);
    }
}