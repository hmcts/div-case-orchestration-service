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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.AllocatedCourt;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CHECK_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ERROR_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_VALIDATION_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

@RunWith(MockitoJUnitRunner.class)
public class OrchestrationControllerTest {

    @Mock
    private CaseOrchestrationService caseOrchestrationService;

    @InjectMocks
    private OrchestrationController classUnderTest;

    @Test
    public void givenNoErrors_whenPetitionIssued_thenCallbackWorksAsExpected() throws WorkflowException {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        CcdCallbackResponse expected = CcdCallbackResponse.builder().data(Collections.emptyMap()).build();

        when(caseOrchestrationService.ccdCallbackHandler(createEvent, AUTH_TOKEN, true))
            .thenReturn(Collections.emptyMap());

        ResponseEntity<CcdCallbackResponse> actual = classUnderTest.petitionIssuedCallback(AUTH_TOKEN,
            true, createEvent);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void givenNoErrors_whenBulkPrintIssued_thenCallbackWorksAsExpected() throws WorkflowException {
        final Map<String, Object> caseData = new HashMap<>();
        Document document = new Document();
        DocumentLink documentLink = new DocumentLink();
        documentLink.setDocumentUrl("http://document.pdf");
        documentLink.setDocumentFilename("document.pdf");
        document.setDocumentLink(documentLink);
        document.setDocumentType("IssuePetition");
        CollectionMember<Document> issuePdf = new CollectionMember<>();
        issuePdf.setValue(document);
        List<CollectionMember<Document>> documents = new ArrayList<>();
        caseData.put("DocumentGenerated", documents);
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        CcdCallbackResponse expected =
            CcdCallbackResponse.builder().errors(Collections.emptyList()).warnings(Collections.emptyList())
                .data(Collections.emptyMap()).build();

        when(caseOrchestrationService.ccdCallbackBulkPrintHandler(createEvent, AUTH_TOKEN))
            .thenReturn(Collections.emptyMap());
        ResponseEntity<CcdCallbackResponse> actual = classUnderTest.bulkPrint(AUTH_TOKEN, createEvent);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }


    @Test
    public void givenErrors_whenPetitionIssued_thenReturnErrorResponse() throws WorkflowException {
        final List<String> expectedError = Collections.singletonList("Some error");
        final Map<String, Object> caseData =
            Collections.singletonMap(
                VALIDATION_ERROR_KEY,
                ValidationResponse.builder()
                    .errors(expectedError)
                    .build());
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .errors(expectedError)
            .build();

        when(caseOrchestrationService.ccdCallbackHandler(createEvent, AUTH_TOKEN, false))
            .thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> actual = classUnderTest.petitionIssuedCallback(AUTH_TOKEN,
            false, createEvent);

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
        final Map<String, Object> serviceReturnData = new HashMap<>();
        serviceReturnData.put(ID, TEST_CASE_ID);
        serviceReturnData.put("allocatedCourt", "randomlySelectedCourt");
        when(caseOrchestrationService.submit(caseData, AUTH_TOKEN)).thenReturn(serviceReturnData);

        ResponseEntity<CaseCreationResponse> response = classUnderTest.submit(AUTH_TOKEN, caseData);

        final CaseCreationResponse expectedResponse = CaseCreationResponse.builder()
                .caseId(TEST_CASE_ID)
                .status(SUCCESS_STATUS)
                .allocatedCourt(new AllocatedCourt("randomlySelectedCourt"))
                .build();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
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

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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

    @Test(expected = WorkflowException.class)
    public void givenThrowsException_whenRetrieveAosCase_thenThrowWorkflowException() throws WorkflowException {

        when(caseOrchestrationService.retrieveAosCase(TEST_CHECK_CCD, AUTH_TOKEN))
            .thenThrow(new WorkflowException("error"));

        classUnderTest.retrieveAosCase(AUTH_TOKEN, TEST_CHECK_CCD);
    }

    @Test
    public void givenAllGoesWell_whenRetrieveAosCase_thenReturnExpectedResponse() throws WorkflowException {
        final CaseDataResponse caseDataResponse = CaseDataResponse.builder().build();

        when(caseOrchestrationService.retrieveAosCase(TEST_CHECK_CCD, AUTH_TOKEN))
            .thenReturn(caseDataResponse);

        ResponseEntity<CaseDataResponse> actual = classUnderTest.retrieveAosCase(AUTH_TOKEN, TEST_CHECK_CCD);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(caseDataResponse, actual.getBody());

        verify(caseOrchestrationService).retrieveAosCase(TEST_CHECK_CCD, AUTH_TOKEN);
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
        when(caseOrchestrationService.linkRespondent(AUTH_TOKEN, TEST_CASE_ID, PIN)).thenReturn(null);

        assertEquals(HttpStatus.UNAUTHORIZED,
            classUnderTest.linkRespondent(AUTH_TOKEN, TEST_CASE_ID, PIN).getStatusCode());

        verify(caseOrchestrationService).linkRespondent(AUTH_TOKEN, TEST_CASE_ID, PIN);
    }

    @Test
    public void givenLinkResponseIsNotNull_whenLinkRespondent_thenReturnUnAuthorised() throws WorkflowException {
        final UserDetails expected = UserDetails.builder().build();

        when(caseOrchestrationService.linkRespondent(AUTH_TOKEN, TEST_CASE_ID, PIN)).thenReturn(expected);

        ResponseEntity<UserDetails> actual = classUnderTest.linkRespondent(AUTH_TOKEN, TEST_CASE_ID, PIN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());

        verify(caseOrchestrationService).linkRespondent(AUTH_TOKEN, TEST_CASE_ID, PIN);
    }

    @Test
    public void whenPetitionSubmittedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        when(caseOrchestrationService.sendPetitionerSubmissionNotificationEmail(createEvent)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.petitionSubmitted(null, createEvent);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenPetitionUpdatedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();
        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);
        when(caseOrchestrationService.sendPetitionerGenericUpdateNotificationEmail(createEvent)).thenReturn(caseData);
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.petitionUpdated(null, createEvent);
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenRespondentSubmittedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();
        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);
        when(caseOrchestrationService.sendRespondentSubmissionNotificationEmail(createEvent)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.respondentAOSSubmitted(null, createEvent);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenGetPetitionIssueFees_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        when(caseOrchestrationService.setOrderSummary(createEvent)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.getPetitionIssueFees(createEvent);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenProcessPbaPayment_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        when(caseOrchestrationService.processPbaPayment(createEvent, AUTH_TOKEN)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processPbaPayment(AUTH_TOKEN, createEvent);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void givenInvalidData_whenProcessPbaPayment_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final Map<String, Object> invalidResponse = Collections.singletonMap(
            SOLICITOR_VALIDATION_ERROR_KEY,
            Collections.singletonList(ERROR_STATUS)
        );

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        when(caseOrchestrationService.processPbaPayment(createEvent, AUTH_TOKEN)).thenReturn(invalidResponse);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processPbaPayment(AUTH_TOKEN, createEvent);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .errors(Collections.singletonList(ERROR_STATUS))
            .build();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenSolicitorCreate_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        when(caseOrchestrationService.solicitorCreate(createEvent)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solicitorCreate(createEvent);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenSubmitAos_thenProceedAsExpected() throws WorkflowException {
        final Map<String, Object> caseData = Collections.emptyMap();

        when(caseOrchestrationService.submitAosCase(caseData, AUTH_TOKEN, TEST_CASE_ID)).thenReturn(caseData);

        ResponseEntity<Map<String, Object>> response = classUnderTest.submitAos(AUTH_TOKEN, TEST_CASE_ID, caseData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(caseData, response.getBody());

        verify(caseOrchestrationService).submitAosCase(caseData, AUTH_TOKEN, TEST_CASE_ID);
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
    public void whenDNSubmittedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        when(caseOrchestrationService.dnSubmitted(createEvent, AUTH_TOKEN)).thenReturn(expectedResponse);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest
            .dnSubmitted(AUTH_TOKEN, createEvent);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }
}