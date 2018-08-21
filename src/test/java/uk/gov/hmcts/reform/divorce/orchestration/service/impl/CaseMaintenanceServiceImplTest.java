package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseMaintenanceServiceImplTest {

    @Mock
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    @Mock
    private UpdateToCCDWorkflow updateToCCDWorkflow;

    @InjectMocks
    private CaseMaintenanceServiceImpl caseMaintenanceService;

    private String authToken;
    private String caseId;
    private String eventId;
    private Map<String, Object> testData;

    @Before
    public void setup() {
        authToken = "authToken";
        caseId = "1234567890";
        eventId = "eventId";
        testData = new HashMap<>();
        testData.put("id", "test-id");
    }

    @Test
    public void givenCaseDataValid_whenSubmit_thenReturnPayload() throws Exception {
        when(submitToCCDWorkflow.run(testData, authToken)).thenReturn(testData);
        when(submitToCCDWorkflow.errors()).thenReturn(Collections.emptyMap());

        assertEquals(testData, caseMaintenanceService.submit(testData, authToken));

        verify(submitToCCDWorkflow).run(testData, authToken);
        verify(submitToCCDWorkflow).errors();
    }

    @Test
    public void givenCaseDataInvalid_whenSubmit_thenReturnListOfErrors() throws Exception {
        when(submitToCCDWorkflow.run(testData, authToken)).thenReturn(testData);

        Map<String, Object> errors = new HashMap<>();
        errors.put("new_Error", "An Error");
        when(submitToCCDWorkflow.errors()).thenReturn(errors);

        assertEquals(errors, caseMaintenanceService.submit(testData, authToken));

        verify(submitToCCDWorkflow).run(testData, authToken);
        verify(submitToCCDWorkflow, times(2)).errors();
    }

    @Test(expected = WorkflowException.class)
    public void givenCaseDataValid_whenSubmitFails_thenThrowWorkflowException() throws Exception {
        when(submitToCCDWorkflow.run(testData, authToken)).thenThrow(new WorkflowException("An Error"));

        caseMaintenanceService.submit(testData, authToken);

        verify(submitToCCDWorkflow).run(testData, authToken);
    }

    @Test
    public void givenCaseDataValid_whenUpdate_thenReturnPayload() throws Exception {
        when(updateToCCDWorkflow.run(testData, authToken, caseId, eventId)).thenReturn(testData);
        when(updateToCCDWorkflow.errors()).thenReturn(Collections.emptyMap());

        assertEquals(testData, caseMaintenanceService.update(testData, authToken, caseId, eventId));

        verify(updateToCCDWorkflow).run(testData, authToken, caseId, eventId);
        verify(updateToCCDWorkflow).errors();
    }

    @Test
    public void givenCaseDataInvalid_whenUpdate_thenReturnListOfErrors() throws Exception {
        when(updateToCCDWorkflow.run(testData, authToken, caseId, eventId)).thenReturn(testData);

        Map<String, Object> errors = new HashMap<>();
        errors.put("new_Error", "An Error");
        when(updateToCCDWorkflow.errors()).thenReturn(errors);

        assertEquals(errors, caseMaintenanceService.update(testData, authToken, caseId, eventId));

        verify(updateToCCDWorkflow).run(testData, authToken, caseId, eventId);
        verify(updateToCCDWorkflow, times(2)).errors();
    }

    @Test(expected = WorkflowException.class)
    public void givenCaseDataValid_whenUpdateFails_thenThrowWorkflowException() throws Exception {
        when(updateToCCDWorkflow.run(testData, authToken, caseId, eventId))
                .thenThrow(new WorkflowException("An Error"));

        caseMaintenanceService.update(testData, authToken, caseId, eventId);

        verify(updateToCCDWorkflow).run(testData, authToken, caseId, eventId);
    }
}