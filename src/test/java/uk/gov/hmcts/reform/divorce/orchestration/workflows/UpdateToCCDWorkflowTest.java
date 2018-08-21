package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.http.auth.AUTH;
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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateToCCDWorkflowTest {

    private static final String AUTH_TOKEN_KEY = "authToken";
    private static final String CASE_ID_KEY = "caseId";
    private static final String EVENT_ID_KEY = "eventId";

    @Mock
    FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Mock
    UpdateCaseInCCD updateCaseInCCD;

    @InjectMocks
    UpdateToCCDWorkflow updateToCCDWorkflow;

    private String authToken;
    private Map<String, Object> testData;
    private TaskContext context;
    private String caseId;
    private String eventId;

    @Before
    public void setup() {
        authToken = "authToken";
        caseId = "1234567890";
        eventId = "updateEvent";
        testData = Collections.emptyMap();
        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_KEY, authToken);
        context.setTransientObject(CASE_ID_KEY, caseId);
        context.setTransientObject(EVENT_ID_KEY, eventId);
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        when(formatDivorceSessionToCaseData.execute(context, testData)).thenReturn(testData);
        when(updateCaseInCCD.execute(context, testData)).thenReturn(testData);

        assertEquals(testData, updateToCCDWorkflow.run(testData, authToken, caseId, eventId));

        verify(formatDivorceSessionToCaseData).execute(context, testData);
        verify(updateCaseInCCD).execute(context, testData);
    }

    @Test(expected = WorkflowException.class)
    public void runShouldThrowWorkflowExceptionWhenTaskExceptionIsThrown() throws Exception {
        when(formatDivorceSessionToCaseData.execute(context, testData)).thenThrow(new TaskException("An Error"));

        updateToCCDWorkflow.run(testData, authToken, caseId, eventId);

        verify(formatDivorceSessionToCaseData).execute(context, testData);
    }
}
