package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SubmitToCCDWorkflowTest {

    @Mock
    FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Mock
    ValidateCaseData validateCaseData;

    @Mock
    SubmitCaseToCCD submitCaseToCCD;

    @InjectMocks
    SubmitToCCDWorkflow submitToCCDWorkflow;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = Collections.emptyMap();
        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        Map<String, Object> resultData = Collections.singletonMap("Hello", "World");

        when(formatDivorceSessionToCaseData.execute(context, testData)).thenReturn(testData);
        when(validateCaseData.execute(context, testData)).thenReturn(testData);
        when(submitCaseToCCD.execute(context, testData)).thenReturn(resultData);

        assertEquals(resultData, submitToCCDWorkflow.run(testData, AUTH_TOKEN));

        verify(formatDivorceSessionToCaseData).execute(context, testData);
        verify(validateCaseData).execute(context, testData);
        verify(submitCaseToCCD).execute(context, testData);
    }

    @Test(expected = WorkflowException.class)
    public void runShouldThrowWorkflowExceptionWhenTaskExceptionIsThrown() throws Exception {
        when(formatDivorceSessionToCaseData.execute(context, testData)).thenThrow(new TaskException("An Error"));

        submitToCCDWorkflow.run(testData, AUTH_TOKEN);

        verify(formatDivorceSessionToCaseData).execute(context, testData);
    }
}
