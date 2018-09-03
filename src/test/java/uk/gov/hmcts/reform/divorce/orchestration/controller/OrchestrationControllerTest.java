package uk.gov.hmcts.reform.divorce.orchestration.controller;

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
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

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

        CcdCallbackResponse expected = CcdCallbackResponse.builder().data(new HashMap<>()).build();

        when(caseOrchestrationService.ccdCallbackHandler(createEvent, AUTH_TOKEN)).thenReturn(new HashMap<>());

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
}