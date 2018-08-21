package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseMaintenanceService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseMaintenanceControllerTest {

    @Mock
    private CaseMaintenanceService caseMaintenanceService;

    @InjectMocks
    private CaseMaintenanceController caseMaintenanceController;

    private String authToken;
    private String caseId;
    private String eventId;

    @Before
    public void setup() {
        authToken = "authToken";
        caseId = "1234567890";
        eventId = "eventId";
    }

    @Test
    public void whenSubmit_thenReturnPayload() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("hello", "world");

        when(caseMaintenanceService.submit(caseData, authToken)).thenReturn(expectedData);

        ResponseEntity<Map<String, Object>> response = caseMaintenanceController.submit(authToken, caseData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedData, response.getBody());
    }

    @Test
    public void whenSubmit_givenException_thenReturnPayload() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();

        when(caseMaintenanceService.submit(caseData, authToken)).thenThrow(new WorkflowException("An Error"));

        ResponseEntity<Map<String, Object>> response = caseMaintenanceController.submit(authToken, caseData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(caseData, response.getBody());
    }

    @Test
    public void whenUpdate_thenReturnPayload() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("hello", "world");

        when(caseMaintenanceService.update(caseData, authToken, caseId, eventId)).thenReturn(expectedData);

        ResponseEntity<Map<String, Object>> response = caseMaintenanceController
                .update(authToken, caseId, eventId, caseData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedData, response.getBody());
    }

    @Test
    public void whenUpdate_givenException_thenReturnPayload() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();

        when(caseMaintenanceService.update(caseData, authToken, caseId, eventId))
                .thenThrow(new WorkflowException("An Error"));

        ResponseEntity<Map<String, Object>> response = caseMaintenanceController
                .update(authToken, caseId, eventId, caseData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(caseData, response.getBody());
    }
}