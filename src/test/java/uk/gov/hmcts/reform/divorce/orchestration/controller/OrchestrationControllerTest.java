package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

@RunWith(MockitoJUnitRunner.class)
public class OrchestrationControllerTest {

    @Mock
    private CaseOrchestrationService caseOrchestrationService;

    @InjectMocks
    private OrchestrationController classUnderTest;

    @Test
    public void whenPetitionIssued_thenCallbackWorksAsExpected() throws WorkflowException {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        CcdCallbackResponse expected = CcdCallbackResponse.builder().data(Collections.emptyMap()).build();

        when(caseOrchestrationService.ccdCallbackHandler(createEvent, AUTH_TOKEN)).thenReturn(Collections.emptyMap());

        ResponseEntity<CcdCallbackResponse> actual = classUnderTest.petitionIssuedCallback(AUTH_TOKEN, createEvent);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void givenWorkflowExceptionThrown_whenAuthenticateRespondent_thenReturnUnauthorized() throws WorkflowException {
        when(caseOrchestrationService.authenticateRespondent(AUTH_TOKEN)).thenThrow(new WorkflowException(""));

        ResponseEntity<Void> actual = classUnderTest.authenticateRespondent(AUTH_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, actual.getStatusCode());

        verify(caseOrchestrationService).authenticateRespondent(AUTH_TOKEN);
    }

    @Test
    public void givenResponseIsNull_whenAuthenticateRespondent_thenReturnUnauthorized() throws WorkflowException {
        when(caseOrchestrationService.authenticateRespondent(AUTH_TOKEN)).thenReturn(null);

        ResponseEntity<Void> actual = classUnderTest.authenticateRespondent(AUTH_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, actual.getStatusCode());

        verify(caseOrchestrationService).authenticateRespondent(AUTH_TOKEN);
    }

    @Test
    public void givenResponseIsFalse_whenAuthenticateRespondent_thenReturnUnauthorized() throws WorkflowException {
        when(caseOrchestrationService.authenticateRespondent(AUTH_TOKEN)).thenReturn(false);

        ResponseEntity<Void> actual = classUnderTest.authenticateRespondent(AUTH_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, actual.getStatusCode());

        verify(caseOrchestrationService).authenticateRespondent(AUTH_TOKEN);
    }

    @Test
    public void givenResponseIsTrue_whenAuthenticateRespondent_thenReturnOK() throws WorkflowException {
        when(caseOrchestrationService.authenticateRespondent(AUTH_TOKEN)).thenReturn(true);

        ResponseEntity<Void> actual = classUnderTest.authenticateRespondent(AUTH_TOKEN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());

        verify(caseOrchestrationService).authenticateRespondent(AUTH_TOKEN);
    }

    @Test
    public void whenSubmit_thenReturnCaseResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> submissionData = Collections.singletonMap(ID, TEST_CASE_ID);
        final CaseResponse expectedResponse = CaseResponse.builder()
                .caseId(TEST_CASE_ID)
                .status(SUCCESS_STATUS)
                .build();

        when(caseOrchestrationService.submit(caseData, AUTH_TOKEN)).thenReturn(submissionData);

        ResponseEntity<CaseResponse> response = classUnderTest.submit(AUTH_TOKEN, caseData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenSubmit_givenException_thenReturnInternalServerError() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();

        when(caseOrchestrationService.submit(caseData, AUTH_TOKEN)).thenThrow(new WorkflowException("An Error"));

        ResponseEntity<CaseResponse> response = classUnderTest.submit(AUTH_TOKEN, caseData);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void whenSubmit_givenErrors_thenReturnBadRequest() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> invalidResponse = Collections.singletonMap(
                VALIDATION_ERROR_KEY,
                ValidationResponse.builder().build()
        );

        when(caseOrchestrationService.submit(caseData, AUTH_TOKEN)).thenReturn(invalidResponse);

        ResponseEntity<CaseResponse> response = classUnderTest.submit(AUTH_TOKEN, caseData);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void whenUpdate_thenReturnPayload() throws Exception {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(CASE_EVENT_DATA_JSON_KEY,  Collections.emptyMap());
        eventData.put(CASE_EVENT_ID_JSON_KEY, TEST_EVENT_ID);

        final Map<String, Object> submissionData = Collections.singletonMap(ID, TEST_CASE_ID);
        final CaseResponse expectedResponse = CaseResponse.builder()
                .caseId(TEST_CASE_ID)
                .status(SUCCESS_STATUS)
                .build();

        when(caseOrchestrationService.update(eventData, AUTH_TOKEN, TEST_CASE_ID))
                .thenReturn(submissionData);

        ResponseEntity<CaseResponse> response = classUnderTest
                .update(AUTH_TOKEN, TEST_CASE_ID, eventData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenUpdate_givenException_thenReturnInternalServerError() throws Exception {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(CASE_EVENT_DATA_JSON_KEY,  Collections.emptyMap());
        eventData.put(CASE_EVENT_ID_JSON_KEY, TEST_EVENT_ID);

        when(caseOrchestrationService.update(eventData, AUTH_TOKEN, TEST_CASE_ID))
                .thenThrow(new WorkflowException("An Error"));

        ResponseEntity<CaseResponse> response = classUnderTest
                .update(AUTH_TOKEN, TEST_CASE_ID, eventData);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}