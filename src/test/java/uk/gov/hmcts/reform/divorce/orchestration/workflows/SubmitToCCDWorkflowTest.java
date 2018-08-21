package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
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

    private String authToken;
    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        authToken = "authToken";
        testData = Collections.emptyMap();
        context = new DefaultTaskContext();
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        when(formatDivorceSessionToCaseData.execute(context, testData)).thenReturn(testData);
        when(validateCaseData.execute(context, testData)).thenReturn(testData);
        when(submitCaseToCCD.execute(context, testData)).thenReturn(testData);

        assertEquals(testData, submitToCCDWorkflow.run(testData, authToken));

        verify(formatDivorceSessionToCaseData).execute(context, testData);
        verify(validateCaseData).execute(context, testData);
        verify(submitCaseToCCD).execute(context, testData);
    }

    @Test(expected = WorkflowException.class)
    public void runShouldThrowWorkflowExceptionWhenTaskExceptionIsThrown() throws Exception {
        when(formatDivorceSessionToCaseData.execute(context, testData)).thenThrow(new TaskException("An Error"));

        submitToCCDWorkflow.run(testData, authToken);

        verify(formatDivorceSessionToCaseData).execute(context, testData);
    }
}
