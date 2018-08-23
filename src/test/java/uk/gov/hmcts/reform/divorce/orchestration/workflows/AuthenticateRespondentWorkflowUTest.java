package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AuthenticateRespondent;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticateRespondentWorkflowUTest {
    private static final String AUTH_TOKEN = "authtoken";

    @Mock
    private AuthenticateRespondent authenticateRespondent;

    @Mock
    private DefaultTaskContext defaultTaskContext;

    @InjectMocks
    private AuthenticateRespondentWorkflow classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "context", defaultTaskContext);
    }

    @Test
    public void whenRun_thenProceedAsExpected() throws Exception {
        final boolean expected = true;

        Mockito.when(authenticateRespondent.execute(defaultTaskContext, null, AUTH_TOKEN)).thenReturn(expected);

        classUnderTest.run(AUTH_TOKEN);

        Mockito.verify(authenticateRespondent).execute(defaultTaskContext, null, AUTH_TOKEN);
    }
}