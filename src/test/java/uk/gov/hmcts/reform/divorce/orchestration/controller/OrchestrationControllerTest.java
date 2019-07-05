package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseCreationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.ALLOCATED_COURT_KEY;

@RunWith(MockitoJUnitRunner.class)
public class OrchestrationControllerTest {

    @Mock
    private CaseOrchestrationService caseOrchestrationService;

    @InjectMocks
    private OrchestrationController classUnderTest;

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
        Court mockAllocatedCourt = new Court();
        String testCourtId = "randomlySelectedCourt";
        mockAllocatedCourt.setCourtId(testCourtId);

        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> serviceReturnData = new HashMap<>();
        serviceReturnData.put(ID, TEST_CASE_ID);
        serviceReturnData.put(ALLOCATED_COURT_KEY, mockAllocatedCourt);
        when(caseOrchestrationService.submit(caseData, AUTH_TOKEN)).thenReturn(serviceReturnData);

        ResponseEntity<CaseCreationResponse> response = classUnderTest.submit(AUTH_TOKEN, caseData);

        CaseCreationResponse responseBody = response.getBody();
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(responseBody.getCaseId(), equalTo(TEST_CASE_ID));
        assertThat(responseBody.getStatus(), equalTo(SUCCESS_STATUS));
        assertThat(responseBody.getAllocatedCourt(), equalTo(mockAllocatedCourt));
    }

    @Test
    public void givenErrors_whenSubmit_thenReturnBadRequest() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> invalidResponse = Collections.singletonMap(
            VALIDATION_ERROR_KEY,
            ValidationResponse.builder().build()
        );

        when(caseOrchestrationService.submit(caseData, AUTH_TOKEN)).thenReturn(invalidResponse);

        ResponseEntity response = classUnderTest.submit(AUTH_TOKEN, caseData);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void whenUpdate_thenReturnCaseResponse() throws Exception {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(CASE_EVENT_DATA_JSON_KEY, Collections.emptyMap());
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
    public void whenPaymentUpdate_thenReturnCaseResponse() throws Exception {

        final Map<String, Object> submissionData = Collections.singletonMap(ID, TEST_CASE_ID);
        PaymentUpdate paymentUpdate = new PaymentUpdate();
        paymentUpdate.setStatus("success");
        paymentUpdate.setCaseReference("123123");
        when(caseOrchestrationService.update(paymentUpdate))
            .thenReturn(submissionData);

        ResponseEntity<CaseResponse> response = classUnderTest
            .paymentUpdate(paymentUpdate);
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test(expected = WorkflowException.class)
    public void givenThrowsException_whenRetrieveAosCase_thenThrowWorkflowException() throws WorkflowException {

        when(caseOrchestrationService.retrieveAosCase(AUTH_TOKEN))
            .thenThrow(new WorkflowException("error"));

        classUnderTest.retrieveAosCase(AUTH_TOKEN);
    }

    @Test
    public void givenAllGoesWell_whenRetrieveAosCase_thenReturnExpectedResponse() throws WorkflowException {
        final CaseDataResponse caseDataResponse = CaseDataResponse.builder().build();

        when(caseOrchestrationService.retrieveAosCase(AUTH_TOKEN))
            .thenReturn(caseDataResponse);

        ResponseEntity<CaseDataResponse> actual = classUnderTest.retrieveAosCase(AUTH_TOKEN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(caseDataResponse, actual.getBody());

        verify(caseOrchestrationService).retrieveAosCase(AUTH_TOKEN);
    }

    @Test
    public void whenGetCase_thenReturnExpectedResponse() throws WorkflowException {
        final CaseDataResponse caseDataResponse = CaseDataResponse.builder().build();

        when(caseOrchestrationService.getCase(AUTH_TOKEN)).thenReturn(caseDataResponse);

        ResponseEntity<CaseDataResponse> response = classUnderTest.retrieveCase(AUTH_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(caseDataResponse, response.getBody());

        verify(caseOrchestrationService).getCase(AUTH_TOKEN);
    }

    @Test(expected = WorkflowException.class)
    public void givenThrowsException_whenGetCase_thenReturnExpectedResponse() throws WorkflowException {
        when(caseOrchestrationService.getCase(AUTH_TOKEN))
            .thenThrow(new WorkflowException("error"));

        classUnderTest.retrieveCase(AUTH_TOKEN);
    }

    @Test
    public void givenLinkResponseIsNull_whenLinkRespondent_thenReturnUnAuthorised() throws WorkflowException {
        when(caseOrchestrationService.linkRespondent(AUTH_TOKEN, TEST_CASE_ID, RESPONDENT_PIN)).thenReturn(null);

        assertEquals(HttpStatus.UNAUTHORIZED,
            classUnderTest.linkRespondent(AUTH_TOKEN, TEST_CASE_ID, RESPONDENT_PIN).getStatusCode());

        verify(caseOrchestrationService).linkRespondent(AUTH_TOKEN, TEST_CASE_ID, RESPONDENT_PIN);
    }

    @Test
    public void givenLinkResponseIsNotNull_whenLinkRespondent_thenReturnOk() throws WorkflowException {
        final UserDetails expected = UserDetails.builder().build();

        when(caseOrchestrationService.linkRespondent(AUTH_TOKEN, TEST_CASE_ID, RESPONDENT_PIN)).thenReturn(expected);

        ResponseEntity<UserDetails> actual = classUnderTest.linkRespondent(AUTH_TOKEN, TEST_CASE_ID, RESPONDENT_PIN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());

        verify(caseOrchestrationService).linkRespondent(AUTH_TOKEN, TEST_CASE_ID, RESPONDENT_PIN);
    }

    @Test
    public void whenGetPetitionIssueFees_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(caseOrchestrationService.setOrderSummary(ccdCallbackRequest)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.getPetitionIssueFees(ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenSubmitRespondentAos_thenProceedAsExpected() throws WorkflowException {
        final Map<String, Object> caseData = Collections.emptyMap();

        when(caseOrchestrationService.submitRespondentAosCase(caseData, AUTH_TOKEN, TEST_CASE_ID)).thenReturn(caseData);

        ResponseEntity<Map<String, Object>> response = classUnderTest.submitRespondentAos(AUTH_TOKEN, TEST_CASE_ID, caseData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(caseData, response.getBody());

        verify(caseOrchestrationService).submitRespondentAosCase(caseData, AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void whenSubmitCoRespondentAos_thenProceedAsExpected() throws WorkflowException {
        final Map<String, Object> caseData = Collections.emptyMap();

        when(caseOrchestrationService.submitCoRespondentAosCase(caseData, AUTH_TOKEN)).thenReturn(caseData);

        ResponseEntity<Map<String, Object>> response = classUnderTest.submitCoRespondentAos(AUTH_TOKEN, caseData);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(caseData));

        verify(caseOrchestrationService).submitCoRespondentAosCase(caseData, AUTH_TOKEN);
    }

    @Test
    public void whenSubmitDn_thenProceedAsExpected() throws WorkflowException {
        final Map<String, Object> dnCase = Collections.emptyMap();

        when(caseOrchestrationService.submitDnCase(dnCase, AUTH_TOKEN, TEST_CASE_ID)).thenReturn(dnCase);

        ResponseEntity<Map<String, Object>> response = classUnderTest.submitDn(AUTH_TOKEN, TEST_CASE_ID, dnCase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dnCase, response.getBody());

        verify(caseOrchestrationService).submitDnCase(dnCase, AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void whenAmendPetition_thenReturnDraftAndUpdateState() throws Exception {
        final String caseId = "test.id";
        final Map<String, Object> caseData = Collections.singletonMap(
            "previousCaseId", caseId
        );

        when(caseOrchestrationService.amendPetition(caseId, AUTH_TOKEN)).thenReturn(caseData);

        ResponseEntity<Map<String, Object>> response = classUnderTest
            .amendPetition(AUTH_TOKEN, caseId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(caseData, response.getBody());
    }

    @Test
    public void whenCoRespondentSubmittedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();
        when(caseOrchestrationService.sendCoRespReceivedNotificationEmail(ccdCallbackRequest)).thenReturn(expectedResponse);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.corespReceived(ccdCallbackRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void testServiceIsCalledAccordingly_ForMakeCaseEligibleForDA() throws CaseOrchestrationServiceException {
        ResponseEntity<Map<String, Object>> response = classUnderTest.makeCaseEligibleForDecreeAbsolute("testAuthToken", "testCaseId");

        verify(caseOrchestrationService).makeCaseEligibleForDA("testAuthToken", "testCaseId");
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void testFailure_WhenExceptionIsThrown_ForMakeCaseEligibleForDA() throws CaseOrchestrationServiceException {
        when(caseOrchestrationService.makeCaseEligibleForDA("testAuthToken", "testCaseId")).thenThrow(CaseOrchestrationServiceException.class);

        ResponseEntity<Map<String, Object>> response = classUnderTest.makeCaseEligibleForDecreeAbsolute("testAuthToken", "testCaseId");

        verify(caseOrchestrationService).makeCaseEligibleForDA("testAuthToken", "testCaseId");
        assertThat(response.getStatusCode(), is(INTERNAL_SERVER_ERROR));
    }

}