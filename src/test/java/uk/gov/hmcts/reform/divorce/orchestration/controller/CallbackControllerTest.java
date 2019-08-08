package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosPackOfflineService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class CallbackControllerTest {

    @Mock
    private CaseOrchestrationService caseOrchestrationService;

    @Mock
    private AosPackOfflineService aosPackOfflineService;

    @InjectMocks
    private CallbackController classUnderTest;

    @Test
    public void whenPetitionerClarificationRequestedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.sendPetitionerClarificationRequestNotification(ccdCallbackRequest)).thenReturn(caseData);

        final ResponseEntity<CcdCallbackResponse> response = classUnderTest.requestClarificationFromPetitioner(ccdCallbackRequest);
        final CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(expectedResponse));
    }

    @Test
    public void whenDNSubmittedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        when(caseOrchestrationService.dnSubmitted(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(expectedResponse);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest
            .dnSubmitted(AUTH_TOKEN, ccdCallbackRequest);

        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenSolicitorCreate_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(caseOrchestrationService.solicitorCreate(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solicitorCreate(AUTH_TOKEN, ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenProcessPbaPayment_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(caseOrchestrationService.solicitorSubmission(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processPbaPayment(AUTH_TOKEN, ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void givenInvalidData_whenProcessPbaPayment_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final Map<String, Object> invalidResponse = Collections.singletonMap(
            OrchestrationConstants.SOLICITOR_VALIDATION_ERROR_KEY,
            Collections.singletonList(OrchestrationConstants.ERROR_STATUS)
        );

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(caseOrchestrationService.solicitorSubmission(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(invalidResponse);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processPbaPayment(AUTH_TOKEN, ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .errors(Collections.singletonList(OrchestrationConstants.ERROR_STATUS))
            .build();

        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenPetitionUpdatedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);
        when(caseOrchestrationService.sendPetitionerGenericUpdateNotificationEmail(ccdCallbackRequest)).thenReturn(caseData);
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.petitionUpdated(null, ccdCallbackRequest);
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();
        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenRespondentSubmittedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);
        when(caseOrchestrationService.sendRespondentSubmissionNotificationEmail(ccdCallbackRequest)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.respondentAOSSubmitted(null, ccdCallbackRequest);

        assertEquals(OK, response.getStatusCode());
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenPetitionSubmittedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(caseOrchestrationService.sendPetitionerSubmissionNotificationEmail(ccdCallbackRequest)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.petitionSubmitted(null, ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenDnPronouncedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(caseOrchestrationService.sendDnPronouncedNotificationEmail(ccdCallbackRequest)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.dnPronounced(null, ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void givenErrors_whenPetitionIssued_thenReturnErrorResponse() throws WorkflowException {
        final List<String> expectedError = Collections.singletonList("Some error");
        final Map<String, Object> caseData =
            Collections.singletonMap(
                OrchestrationConstants.VALIDATION_ERROR_KEY,
                ValidationResponse.builder()
                    .errors(expectedError)
                    .build());
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .errors(expectedError)
            .build();

        when(caseOrchestrationService.handleIssueEventCallback(ccdCallbackRequest, AUTH_TOKEN, false))
            .thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> actual = classUnderTest.petitionIssuedCallback(AUTH_TOKEN,
            false, ccdCallbackRequest);

        assertEquals(OK, actual.getStatusCode());
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

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        CcdCallbackResponse expected =
            CcdCallbackResponse.builder().errors(Collections.emptyList()).warnings(Collections.emptyList())
                .data(Collections.emptyMap()).build();

        when(caseOrchestrationService.ccdCallbackBulkPrintHandler(ccdCallbackRequest, AUTH_TOKEN))
            .thenReturn(Collections.emptyMap());
        ResponseEntity<CcdCallbackResponse> actual = classUnderTest.bulkPrint(AUTH_TOKEN, ccdCallbackRequest);

        assertEquals(OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void givenNoErrors_whenPetitionIssued_thenCallbackWorksAsExpected() throws WorkflowException {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        CcdCallbackResponse expected = CcdCallbackResponse.builder().data(Collections.emptyMap()).build();

        when(caseOrchestrationService.handleIssueEventCallback(ccdCallbackRequest, AUTH_TOKEN, true))
            .thenReturn(Collections.emptyMap());

        ResponseEntity<CcdCallbackResponse> actual = classUnderTest.petitionIssuedCallback(AUTH_TOKEN,
            true, ccdCallbackRequest);

        assertEquals(OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void testCaseLinkedForHearingCallsRightServiceMethod() throws CaseOrchestrationServiceException {
        Map<String, Object> incomingPayload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(incomingPayload)
                .build())
            .build();
        when(caseOrchestrationService.processCaseLinkedForHearingEvent(incomingRequest)).thenReturn(incomingPayload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.caseLinkedForHearing(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(equalTo(incomingPayload)));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void shouldReturnOkResponse_WithErrors_AndNoCaseData_WhenExceptionIsCaught() throws CaseOrchestrationServiceException {
        Map<String, Object> incomingPayload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(incomingPayload)
                .build())
            .build();
        when(caseOrchestrationService.processCaseLinkedForHearingEvent(incomingRequest))
            .thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.caseLinkedForHearing(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(nullValue()));
        assertThat(response.getBody().getErrors(), hasItem("This is a test error message."));
    }

    @Test
    public void whenCoRespondentAnswerReceived_thenExecuteService() throws Exception {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(payload)
                .build())
            .build();
        when(caseOrchestrationService.coRespondentAnswerReceived(incomingRequest)).thenReturn(payload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.respondentAnswerReceived(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
    }

    @Test
    public void testSolDnReviewPetition() throws CaseOrchestrationServiceException {
        Map<String, Object> incomingPayload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(incomingPayload)
                .build())
            .build();

        when(caseOrchestrationService
            .processSolDnDoc(incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_PETITION, OrchestrationConstants.MINI_PETITION_LINK)
        ).thenReturn(incomingPayload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solDnReviewPetition(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(equalTo(incomingPayload)));
        assertThat(response.getBody().getErrors(), is(nullValue()));
        verify(caseOrchestrationService)
            .processSolDnDoc(incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_PETITION, OrchestrationConstants.MINI_PETITION_LINK);
    }

    @Test
    public void testSolDnReviewPetitionPopulatesErrorsIfExceptionIsThrown() throws CaseOrchestrationServiceException {
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .build())
            .build();

        when(caseOrchestrationService
            .processSolDnDoc(incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_PETITION, OrchestrationConstants.MINI_PETITION_LINK)
        ).thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solDnReviewPetition(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(nullValue()));
        assertThat(response.getBody().getErrors(), hasItem("This is a test error message."));
        verify(caseOrchestrationService)
            .processSolDnDoc(incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_PETITION, OrchestrationConstants.MINI_PETITION_LINK);
    }

    @Test
    public void testSolDnRespAnswersDoc() throws CaseOrchestrationServiceException {
        Map<String, Object> incomingPayload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(incomingPayload)
                .build())
            .build();

        when(caseOrchestrationService
            .processSolDnDoc(incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_ANSWERS, OrchestrationConstants.RESP_ANSWERS_LINK)
        ).thenReturn(incomingPayload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solDnRespAnswersDoc(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(equalTo(incomingPayload)));
        assertThat(response.getBody().getErrors(), is(nullValue()));
        verify(caseOrchestrationService)
            .processSolDnDoc(incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_ANSWERS, OrchestrationConstants.RESP_ANSWERS_LINK);
    }

    @Test
    public void testSolDnRespAnswersDocPopulatesErrorsIfExceptionIsThrown() throws CaseOrchestrationServiceException {
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .build())
            .build();

        when(caseOrchestrationService
            .processSolDnDoc(incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_ANSWERS, OrchestrationConstants.RESP_ANSWERS_LINK)
        ).thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solDnRespAnswersDoc(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(nullValue()));
        assertThat(response.getBody().getErrors(), hasItem("This is a test error message."));
        verify(caseOrchestrationService)
            .processSolDnDoc(incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_ANSWERS, OrchestrationConstants.RESP_ANSWERS_LINK);
    }

    @Test
    public void testSolDnCoRespAnswersDoc() throws CaseOrchestrationServiceException {
        Map<String, Object> incomingPayload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(incomingPayload)
                .build())
            .build();

        when(caseOrchestrationService
            .processSolDnDoc(incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS, OrchestrationConstants.CO_RESP_ANSWERS_LINK)
        ).thenReturn(incomingPayload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solDnCoRespAnswersDoc(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(equalTo(incomingPayload)));
        assertThat(response.getBody().getErrors(), is(nullValue()));
        verify(caseOrchestrationService).processSolDnDoc(
            incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS, OrchestrationConstants.CO_RESP_ANSWERS_LINK
        );
    }

    @Test
    public void testSolDnCoRespAnswersDocPopulatesErrorsIfExceptionIsThrown() throws CaseOrchestrationServiceException {
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .build())
            .build();

        when(caseOrchestrationService
            .processSolDnDoc(incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS, OrchestrationConstants.CO_RESP_ANSWERS_LINK)
        ).thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solDnCoRespAnswersDoc(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(nullValue()));
        assertThat(response.getBody().getErrors(), hasItem("This is a test error message."));
        verify(caseOrchestrationService).processSolDnDoc(
            incomingRequest, OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS, OrchestrationConstants.CO_RESP_ANSWERS_LINK
        );
    }

    @Test
    public void whenGenerateCoRespondentAnswers_thenExecuteService() throws Exception {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(payload)
                .build())
            .build();
        when(caseOrchestrationService.generateCoRespondentAnswers(incomingRequest, AUTH_TOKEN)).thenReturn(payload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateCoRespondentAnswers(AUTH_TOKEN, incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
    }

    @Test
    public void givenWorkflowException_whenGenerateCoRespondentAnswers_thenReturnErrors() throws Exception {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(payload)
                .build())
            .build();

        String errorString = "Unable to generate answers";
        when(caseOrchestrationService.generateCoRespondentAnswers(incomingRequest, AUTH_TOKEN))
            .thenThrow(new WorkflowException(errorString));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateCoRespondentAnswers(AUTH_TOKEN, incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getErrors().contains(errorString), is(true));
    }

    @Test
    public void whenGenerateDocument_thenExecuteService() throws Exception {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(payload)
                .build())
            .build();

        when(caseOrchestrationService
            .handleDocumentGenerationCallback(incomingRequest, AUTH_TOKEN, "a", "b", "c")).thenReturn(payload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateDocument(AUTH_TOKEN, "a", "b", "c",
            incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
    }

    @Test
    public void givenWorkflowException_whenGenerateDocuments_thenReturnErrors() throws WorkflowException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(payload)
                .build())
            .build();

        String errorString = "foo";

        when(caseOrchestrationService
            .handleDocumentGenerationCallback(incomingRequest, AUTH_TOKEN, "a", "b", "c"))
            .thenThrow(new WorkflowException(errorString));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateDocument(AUTH_TOKEN, "a", "b", "c",
            incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getErrors(), contains(errorString));
    }

    @Test
    public void whenGenerateDnPronouncedDocuments_thenExecuteService() throws WorkflowException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(payload)
                .build())
            .build();

        when(caseOrchestrationService
            .handleDnPronouncementDocumentGeneration(incomingRequest, AUTH_TOKEN))
            .thenReturn(payload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateDnDocuments(AUTH_TOKEN, incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
    }

    @Test
    public void whenGenerateDaPronouncedDocuments_thenExecuteService() throws WorkflowException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(payload)
                .build())
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateDnDocuments(AUTH_TOKEN, incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
    }

    @Test
    public void givenWorkflowException_whenGenerateDnPronouncedDocuments_thenReturnErrors() throws WorkflowException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(payload)
                .build())
            .build();

        String errorString = "foo";

        when(caseOrchestrationService
            .handleGrantDACallback(incomingRequest, AUTH_TOKEN))
            .thenThrow(new WorkflowException(errorString));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.daAboutToBeGranted(AUTH_TOKEN, incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getErrors(), contains(errorString));
    }

    @Test
    public void givenWorkflowException_whenGenerateDaPronouncedDocuments_thenReturnErrors() throws WorkflowException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(payload)
                .build())
            .build();

        String errorString = "foo";

        when(caseOrchestrationService
            .handleDnPronouncementDocumentGeneration(incomingRequest, AUTH_TOKEN))
            .thenThrow(new WorkflowException(errorString));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateDnDocuments(AUTH_TOKEN, incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getErrors(), contains(errorString));
    }

    @Test
    public void testAosSolicitorNominated() throws CaseOrchestrationServiceException {
        Map<String, Object> incomingPayload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(incomingPayload)
                .build())
            .build();

        when(caseOrchestrationService.processAosSolicitorNominated(incomingRequest)).thenReturn(incomingPayload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.aosSolicitorNominated(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(equalTo(incomingPayload)));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void shouldReturnOk_WithErrors_AndNoCaseData_WhenExceptionIsCaughtInAosSolicitorNominated() throws CaseOrchestrationServiceException {
        Map<String, Object> incomingPayload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(incomingPayload)
                .build())
            .build();
        when(caseOrchestrationService.processAosSolicitorNominated(incomingRequest))
            .thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.aosSolicitorNominated(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(nullValue()));
        assertThat(response.getBody().getErrors(), hasItem("This is a test error message."));
    }

    @Test
    public void testAosSolicitorLinkCase() throws CaseOrchestrationServiceException {
        Map<String, Object> incomingPayload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(incomingPayload)
                .build())
            .build();

        when(caseOrchestrationService.processAosSolicitorLinkCase(incomingRequest, AUTH_TOKEN))
            .thenReturn(incomingPayload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solicitorLinkCase(AUTH_TOKEN, incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(equalTo(incomingPayload)));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void shouldReturnOk_WithErrors_AndNoCaseData_WhenExceptionIsCaughtInAosSolicitorLinkCase() throws CaseOrchestrationServiceException {
        Map<String, Object> incomingPayload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(incomingPayload)
                .build())
            .build();

        when(caseOrchestrationService.processAosSolicitorLinkCase(incomingRequest, AUTH_TOKEN))
            .thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solicitorLinkCase(AUTH_TOKEN, incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(nullValue()));
        assertThat(response.getBody().getErrors(), hasItem("This is a test error message."));
    }

    @Test
    public void givenNoErrors_whenCalculateSeparationFields_thenCallbackWorksAsExpected() throws WorkflowException {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        CcdCallbackResponse expected = CcdCallbackResponse.builder().data(Collections.emptyMap()).build();

        when(caseOrchestrationService.processSeparationFields(ccdCallbackRequest))
            .thenReturn(Collections.emptyMap());

        ResponseEntity<CcdCallbackResponse> actual = classUnderTest.calculateSeparationFields(ccdCallbackRequest);

        assertEquals(OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void givenErrors_whenCalculateSeparationFields_thenReturnErrorResponse() throws WorkflowException {
        final List<String> expectedError = Collections.singletonList("Some error");
        final Map<String, Object> caseData =
            Collections.singletonMap(OrchestrationConstants.VALIDATION_ERROR_KEY, "Some error");
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .errors(expectedError)
            .build();

        when(caseOrchestrationService.processSeparationFields(ccdCallbackRequest))
            .thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> actual = classUnderTest.calculateSeparationFields(ccdCallbackRequest);

        assertEquals(OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    public void testDnAboutToBeGrantedCallsRightServiceMethod() throws CaseOrchestrationServiceException {
        when(caseOrchestrationService.processCaseBeforeDecreeNisiIsGranted(any()))
            .thenReturn(singletonMap("newKey", "newValue"));

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(singletonMap("testKey", "testValue"))
                .build())
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.dnAboutToBeGranted(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), hasEntry("newKey", "newValue"));
        assertThat(response.getBody().getErrors(), is(nullValue()));
        verify(caseOrchestrationService).processCaseBeforeDecreeNisiIsGranted(eq(ccdCallbackRequest));
    }

    @Test
    public void testDnAboutToBeGrantedHandlesServiceException() throws CaseOrchestrationServiceException {
        when(caseOrchestrationService.processCaseBeforeDecreeNisiIsGranted(any()))
            .thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(singletonMap("testKey", "testValue"))
                .build())
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.dnAboutToBeGranted(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), is(nullValue()));
        assertThat(response.getBody().getErrors(), hasItem(equalTo("This is a test error message.")));
        verify(caseOrchestrationService).processCaseBeforeDecreeNisiIsGranted(eq(ccdCallbackRequest));
    }

    @Test
    public void testClearStateCallRightServiceMethod() throws WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(singletonMap("testKey", "testValue"))
                .build())
            .build();

        when(caseOrchestrationService.cleanStateCallback(ccdCallbackRequest, AUTH_TOKEN))
            .thenReturn(singletonMap("newKey", "newValue"));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.clearStateCallback(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), hasEntry("newKey", "newValue"));
        assertThat(response.getBody().getErrors(), is(nullValue()));
        verify(caseOrchestrationService).cleanStateCallback(eq(ccdCallbackRequest), eq(AUTH_TOKEN));
    }

    @Test
    public void testServiceMethodIsCalled_WhenFlagCaseAsEligibleForDecreeAbsoluteForPetitioner() throws CaseOrchestrationServiceException {
        when(caseOrchestrationService.processApplicantDecreeAbsoluteEligibility(any())).thenReturn(singletonMap("returnedKey", "returnedValue"));

        CcdCallbackRequest callbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseData(singletonMap("incomingKey", "incomingValue")).build())
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processApplicantDecreeAbsoluteEligibility(callbackRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), hasEntry("returnedKey", "returnedValue"));
        verify(caseOrchestrationService).processApplicantDecreeAbsoluteEligibility(callbackRequest);
    }

    @Test
    public void testFlagCaseAsEligibleForDecreeAbsoluteForPetitioner_HandlesServiceException() throws CaseOrchestrationServiceException {
        when(caseOrchestrationService.processApplicantDecreeAbsoluteEligibility(any()))
            .thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(singletonMap("testKey", "testValue"))
                .build())
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processApplicantDecreeAbsoluteEligibility(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), is(nullValue()));
        assertThat(response.getBody().getErrors(), hasItem(equalTo("This is a test error message.")));
        verify(caseOrchestrationService).processApplicantDecreeAbsoluteEligibility(eq(ccdCallbackRequest));
    }

    @Test
    public void testRemoveCaseLink_callServiceMethod() throws WorkflowException {
        CcdCallbackRequest request = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails
                .builder()
                .caseId(TEST_CASE_ID)
                .build()).build();
        Map<String, Object> expectedResponse = singletonMap("returnedKey", "returnedValue");
        when(caseOrchestrationService.removeBulkLink(request)).thenReturn(expectedResponse);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.removeBulkLinkFromCase(request);
        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().data(expectedResponse).build()));
    }

    @Test
    public void testIssueAosOffline_ForRespondent_callsRightService() throws CaseOrchestrationServiceException {
        when(aosPackOfflineService.issueAosPackOffline(any(), any(), any())).thenReturn(singletonMap("returnedKey", "returnedValue"));

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.issueAosPackOffline(AUTH_TOKEN, ccdCallbackRequest, RESPONDENT);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), hasEntry("returnedKey", "returnedValue"));
        verify(aosPackOfflineService).issueAosPackOffline(eq(AUTH_TOKEN), eq(caseDetails), eq(RESPONDENT));
    }

    @Test
    public void testIssueAosOffline_ForCoRespondent_callsRightService() throws CaseOrchestrationServiceException {
        when(aosPackOfflineService.issueAosPackOffline(any(), any(), any())).thenReturn(singletonMap("returnedKey", "returnedValue"));

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.issueAosPackOffline(AUTH_TOKEN, ccdCallbackRequest, CO_RESPONDENT);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), hasEntry("returnedKey", "returnedValue"));
        verify(aosPackOfflineService).issueAosPackOffline(eq(AUTH_TOKEN), eq(caseDetails), eq(CO_RESPONDENT));
    }

    @Test
    public void testIssueAosOffline_returnsErrors_whenServiceThrowsException() throws CaseOrchestrationServiceException {
        when(aosPackOfflineService.issueAosPackOffline(any(), any(), any()))
            .thenThrow(new CaseOrchestrationServiceException(new RuntimeException("Error message")));

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.issueAosPackOffline(AUTH_TOKEN, ccdCallbackRequest, RESPONDENT);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getErrors(), hasItem(equalTo("Error message")));
        assertThat(response.getBody().getData(), is(nullValue()));
    }

}