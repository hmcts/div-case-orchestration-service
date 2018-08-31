package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DELETE_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SAVE_DRAFT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

@RunWith(MockitoJUnitRunner.class)
public class OrchestrationControllerTest {

    private static final String DRAFT_KEY = "draft";
    private static final String AUTH_TOKEN = "authtoken";

    @Mock
    private CaseOrchestrationService service;

    @InjectMocks
    private OrchestrationController controller;

    @Test
    public void whenPetitionIssued_thenCallbackWorksAsExpected() throws WorkflowException {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        CcdCallbackResponse expected = CcdCallbackResponse.builder().data(new HashMap<>()).build();

        when(service.ccdCallbackHandler(createEvent, AUTH_TOKEN)).thenReturn(new HashMap<>());

        ResponseEntity<CcdCallbackResponse> actual = controller.petitionIssuedCallback(AUTH_TOKEN, createEvent);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void givenUserWithoutDraft_whenRetrieveDraft_thenNotFoundResponseReturned() throws WorkflowException {

        ResponseEntity<Map<String, Object>> actual = controller.retrieveDraft(AUTH_TOKEN);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void givenAnErrorOdDraft_whenRetrieveDraft_thenNotFoundResponseReturned() throws WorkflowException {
        final Map<String, Object> draftServiceResponse = new LinkedHashMap<>();
        draftServiceResponse.put(VALIDATION_ERROR_KEY,"Workflow error");
        when(service.getDraft(AUTH_TOKEN)).thenReturn(draftServiceResponse);

        ResponseEntity<Map<String, Object>> actual = controller.retrieveDraft(AUTH_TOKEN);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void givenUserSavedDraft_whenRetrieveDraft_thenUserSaveDraftIsReturned() throws WorkflowException {
        final Map<String, Object> expectedObject = mock(Map.class);
        when(service.getDraft(AUTH_TOKEN)).thenReturn(expectedObject);
        ResponseEntity<Map<String, Object>> actual = controller.retrieveDraft(AUTH_TOKEN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expectedObject, actual.getBody());
    }

    @Test(expected = RuntimeException.class)
    public void givenWorkflowError_whenRetrieveDraft_thenHandleError() throws WorkflowException {

        when(service.getDraft(AUTH_TOKEN)).thenThrow(new WorkflowException("Workflow failed"));
        ResponseEntity<Map<String, Object>> actual = controller.retrieveDraft(AUTH_TOKEN);
    }

    @Test
    public void givenDraftPayload_whenSaveDraft_thenSaveDraftServiceIsCalled() throws WorkflowException {
        Map<String, Object> payload =  mock(Map.class);
        final String userEmail = "test@email.com";

        ResponseEntity<Map<String, Object>> response = controller.saveDraft(AUTH_TOKEN, payload, userEmail, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service).saveDraft(payload, AUTH_TOKEN, userEmail, true);
    }

    @Test
    public void givenWorkflowError_whenSaveDraft_thenReturnError() throws WorkflowException {
        Map<String, Object> payload =  new HashMap<>();
        final String userEmail = "test@email.com";
        WorkflowException safeDraftError = new WorkflowException("Workflow failed");
        when(service.saveDraft(payload, AUTH_TOKEN, userEmail, true)).thenThrow(safeDraftError);
        ResponseEntity<Map<String, Object>> response = controller.saveDraft(AUTH_TOKEN, payload, userEmail, true);

        assertEquals(safeDraftError, response.getBody().get(SAVE_DRAFT_ERROR_KEY));
    }

    @Test
    public void whenDeleteDraft_thenDeleteDraftServiceIsCalled() throws WorkflowException {
        Map<String, Object> payload =  mock(Map.class);
        when(service.deleteDraft(AUTH_TOKEN)).thenReturn(payload);
        ResponseEntity<Map<String, Object>> response = controller.deleteDraft(AUTH_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(payload, response.getBody());

        verify(service).deleteDraft(AUTH_TOKEN);
    }

    @Test
    public void givenWorkflowError_whenDeleteDraft_thenReturnError() throws WorkflowException {
        WorkflowException deleteDraftError = new WorkflowException("Workflow failed");

        when(service.deleteDraft(AUTH_TOKEN)).thenThrow(deleteDraftError);
        ResponseEntity<Map<String, Object>> response = controller.deleteDraft(AUTH_TOKEN);

        assertEquals(deleteDraftError, response.getBody().get(DELETE_ERROR_KEY));
    }
}