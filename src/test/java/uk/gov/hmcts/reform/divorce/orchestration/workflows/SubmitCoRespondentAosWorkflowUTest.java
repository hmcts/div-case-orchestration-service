package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToAosCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCoRespondentAosCase;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SubmitCoRespondentAosWorkflowUTest {
    @Mock
    private FormatDivorceSessionToAosCaseData formatDivorceSessionToAosCaseData;

    @Mock
    private SubmitCoRespondentAosCase submitCoRespondentAosCase;

    @InjectMocks
    private SubmitCoRespondentAosWorkflow classUnderTest;

    @SuppressWarnings("unchecked")
    @Test
    public void whenSubmitCoRespondentAosCase_thenProcessAsExpected() throws WorkflowException {
        final Map<String, Object> inputData = mock(Map.class);
        final Map<String, Object> expectedOutput = mock(Map.class);

        final ImmutablePair<String, Object> authTokenPair = new ImmutablePair<>(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        final Task[] tasks = new Task[]{
            formatDivorceSessionToAosCaseData,
            submitCoRespondentAosCase
        };

        when(classUnderTest.execute(tasks, inputData, authTokenPair)).thenReturn(expectedOutput);

        assertEquals(expectedOutput, classUnderTest.run(inputData, AUTH_TOKEN));
    }
}
