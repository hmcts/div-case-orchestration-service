package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateCaseInCCDTest {

    @Mock
    CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    UpdateCaseInCCD updateCaseInCCD;

    private String authToken;
    private String caseId;
    private String eventId;
    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        authToken = "authToken";
        caseId = "1234567890";
        eventId = "updateEvent";
        testData = Collections.emptyMap();
        context = new DefaultTaskContext();

        context.setTransientObject("authToken", authToken);
        context.setTransientObject("caseId", caseId);
        context.setTransientObject("eventId", eventId);
    }

    @Test
    public void executeShouldCallCaseFormatterClientTransformToCCDFormat() throws Exception {
        when(caseMaintenanceClient.updateCase(testData, authToken, caseId, eventId)).thenReturn(testData);

        assertEquals(testData, updateCaseInCCD.execute(context, testData));

        verify(caseMaintenanceClient).updateCase(testData, authToken, caseId, eventId);
    }

    @Test(expected=TaskException.class)
    public void executeShouldThrowTaskExceptionWhenTransformToCCDFormatThrowsException() throws Exception {
        when(caseMaintenanceClient.updateCase(testData, authToken, caseId, eventId)).thenThrow(new RuntimeException());

        updateCaseInCCD.execute(context, testData);

        verify(caseMaintenanceClient).updateCase(testData, authToken, caseId, eventId);
    }
}
