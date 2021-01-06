package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.model.response.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentStatus;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AlternativeServiceService;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CourtOrderDocumentsUpdateService;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralReferralService;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyService;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPaymentTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_CASE_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PAYLOAD_TO_RETURN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_OUTCOME_FLAG_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ControllerUtils.ccdRequestWithData;

@RunWith(MockitoJUnitRunner.class)
public class CallbackControllerTest {

    @Mock
    private CaseOrchestrationService caseOrchestrationService;

    @Mock
    private AosService aosService;

    @Mock
    private ServiceJourneyService serviceJourneyService;

    @Mock
    private GeneralOrderService generalOrderService;

    @Mock
    private GeneralEmailService generalEmailService;

    @Mock
    private GeneralReferralService generalReferralService;

    @Mock
    private AlternativeServiceService alternativeServiceService;

    @Mock
    private CourtOrderDocumentsUpdateService courtOrderDocumentsUpdateService;

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
    public void testServiceMethodIsCalled_WhenHandleDnSubmittedCallback() throws WorkflowException {
        when(caseOrchestrationService.handleDnSubmitted(any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        CcdCallbackRequest callbackRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.handleDnSubmitted(callbackRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(caseOrchestrationService).handleDnSubmitted(callbackRequest);
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
    public void whenSolicitorAmendPetitionForRefusal_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(caseOrchestrationService.solicitorAmendPetitionForRefusal(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solicitorAmendPetitionForRefusal(AUTH_TOKEN, ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenProcessPbaPayment_thenReturnCcdResponse() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);
        caseData.put(ProcessPbaPaymentTask.PAYMENT_STATUS, PaymentStatus.SUCCESS.value());

        CaseDetails caseDetails = CaseDetails.builder()
            .state(TEST_STATE)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails).build();

        when(caseOrchestrationService.solicitorSubmission(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processPbaPayment(AUTH_TOKEN, ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .state(CcdStates.SUBMITTED)
            .data(caseData)
            .build();

        CcdCallbackResponse actualCallbackResponse = Optional.ofNullable(response.getBody())
            .orElseGet(() -> CcdCallbackResponse.builder().build());

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(expectedResponse));
        assertThat(actualCallbackResponse.getState(), is(expectedResponse.getState()));
    }

    @Test
    public void whenProcessPbaPayment_AndPaymentTypeNotPBA_thenReturnCcdResponseWithSolicitorAwaitingPaymentConfirmationState() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, "feesHelpWith");

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(caseOrchestrationService.solicitorSubmission(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processPbaPayment(AUTH_TOKEN, ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .state(CcdStates.SOLICITOR_AWAITING_PAYMENT_CONFIRMATION)
            .data(caseData)
            .build();

        CcdCallbackResponse actualCallbackResponse = Optional.ofNullable(response.getBody())
            .orElseGet(() -> CcdCallbackResponse.builder().build());

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(expectedResponse));
        assertThat(actualCallbackResponse.getState(), is(expectedResponse.getState()));
    }

    @Test
    public void givenInvalidData_whenProcessPbaPayment_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final Map<String, Object> invalidResponse = Collections.singletonMap(
            OrchestrationConstants.SOLICITOR_PBA_PAYMENT_ERROR_KEY,
            singletonList(OrchestrationConstants.ERROR_STATUS)
        );

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(caseOrchestrationService.solicitorSubmission(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(invalidResponse);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processPbaPayment(AUTH_TOKEN, ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .errors(singletonList(OrchestrationConstants.ERROR_STATUS))
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
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.petitionUpdated(ccdCallbackRequest);
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

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.petitionSubmitted(ccdCallbackRequest);

        CcdCallbackResponse ccdCallbackResponse = response.getBody();
        assertThat(response.getStatusCode(), is(OK));
        assertThat(ccdCallbackResponse.getData(), is(caseData));
        assertThat(ccdCallbackResponse.getErrors(), is(nullValue()));
    }

    @Test
    public void whenDnPronouncedCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(caseOrchestrationService.sendDnPronouncedNotification(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.dnPronounced(AUTH_TOKEN, ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void givenErrors_whenPetitionIssued_thenReturnErrorResponse() throws WorkflowException {
        final List<String> expectedError = singletonList("Some error");
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
    public void givenNoErrors_whenConfirmServiceCalled_thenCallbackWorksAsExpected() throws WorkflowException {
        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        CcdCallbackResponse expected =
            CcdCallbackResponse.builder().errors(Collections.emptyList()).warnings(Collections.emptyList())
                .data(Collections.emptyMap()).build();

        when(caseOrchestrationService.ccdCallbackConfirmPersonalService(ccdCallbackRequest, AUTH_TOKEN))
            .thenReturn(Collections.emptyMap());
        ResponseEntity<CcdCallbackResponse> actual = classUnderTest.confirmPersonalService(AUTH_TOKEN, ccdCallbackRequest);

        assertEquals(OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void givenErrors_whenConfirmServiceCalled_thenReturnErrorResponse() throws WorkflowException {
        final Map<String, Object> caseData =
            Collections.singletonMap(OrchestrationConstants.BULK_PRINT_ERROR_KEY, "Some error");
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(ImmutableMap.of())
            .warnings(ImmutableList.of())
            .errors(singletonList("Failed to bulk print documents"))
            .build();

        when(caseOrchestrationService.ccdCallbackConfirmPersonalService(ccdCallbackRequest, AUTH_TOKEN))
            .thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> actual = classUnderTest.confirmPersonalService(AUTH_TOKEN, ccdCallbackRequest);

        assertEquals(OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void shouldReturnOkResponse_WithErrors_whenConfirmServiceCalled_thenExceptionIsCaught() throws WorkflowException {
        final Map<String, Object> incomingPayload = new HashMap<>();
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(incomingPayload)
                .build())
            .build();
        String errorString = "Unable to bulk print the documents";
        when(caseOrchestrationService.ccdCallbackConfirmPersonalService(incomingRequest, AUTH_TOKEN))
            .thenThrow(new WorkflowException(errorString));

        WorkflowException workflowException = assertThrows(WorkflowException.class,
            () -> classUnderTest.confirmPersonalService(AUTH_TOKEN, incomingRequest));

        assertThat(workflowException.getMessage(), is(errorString));
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
    public void givenErrors_whenBulkPrintIssued_thenRespondWithOkAndReturnErrors() throws WorkflowException {
        when(caseOrchestrationService.ccdCallbackBulkPrintHandler(any(), any()))
            .thenThrow(new WorkflowException("Error message"));

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.bulkPrint(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getErrors(), hasItem(equalTo("Failed to bulk print documents - Error message")));
        assertThat(response.getBody().getData(), is(Collections.emptyMap()));
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
        when(caseOrchestrationService.processCaseLinkedForHearingEvent(incomingRequest, AUTH_TOKEN))
            .thenReturn(incomingPayload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.caseLinkedForHearing(AUTH_TOKEN, incomingRequest);

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
        when(caseOrchestrationService.processCaseLinkedForHearingEvent(incomingRequest, AUTH_TOKEN))
            .thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.caseLinkedForHearing(AUTH_TOKEN, incomingRequest);

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
                .caseId(TEST_CASE_ID)
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
            .processSolDnDoc(incomingRequest, DocumentType.RESPONDENT_ANSWERS.getTemplateLogicalName(),
                OrchestrationConstants.RESP_ANSWERS_LINK)
        ).thenReturn(incomingPayload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solDnRespAnswersDoc(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(equalTo(incomingPayload)));
        assertThat(response.getBody().getErrors(), is(nullValue()));
        verify(caseOrchestrationService)
            .processSolDnDoc(incomingRequest, DocumentType.RESPONDENT_ANSWERS.getTemplateLogicalName(), OrchestrationConstants.RESP_ANSWERS_LINK);
    }

    @Test
    public void testSolDnRespAnswersDocPopulatesErrorsIfExceptionIsThrown() throws CaseOrchestrationServiceException {
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .build())
            .build();

        when(caseOrchestrationService
            .processSolDnDoc(incomingRequest, DocumentType.RESPONDENT_ANSWERS.getTemplateLogicalName(), OrchestrationConstants.RESP_ANSWERS_LINK)
        ).thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solDnRespAnswersDoc(incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(nullValue()));
        assertThat(response.getBody().getErrors(), hasItem("This is a test error message."));
        verify(caseOrchestrationService)
            .processSolDnDoc(incomingRequest, DocumentType.RESPONDENT_ANSWERS.getTemplateLogicalName(), OrchestrationConstants.RESP_ANSWERS_LINK);
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
                .caseId(TEST_CASE_ID)
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
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();

        when(caseOrchestrationService.handleDocumentGenerationCallback(eq(incomingRequest), eq(AUTH_TOKEN), any(), any(), any()))
            .thenReturn(TEST_INCOMING_CASE_DETAILS.getCaseData());

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateDocument(AUTH_TOKEN, "a", "b", "c", incomingRequest);

        verify(caseOrchestrationService).handleDocumentGenerationCallback(incomingRequest, AUTH_TOKEN, "a", "b", "c");
        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(TEST_INCOMING_CASE_DETAILS.getCaseData()));
    }

    @Test
    public void whenGenerateDocument_whenPreparingToPrintForPronouncement() throws Exception {
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();

        when(caseOrchestrationService.handleDocumentGenerationCallback(eq(incomingRequest), eq(AUTH_TOKEN), any(), any(), any()))
            .thenReturn(TEST_INCOMING_CASE_DETAILS.getCaseData());

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.prepareToPrintForPronouncement(AUTH_TOKEN, incomingRequest);

        verify(caseOrchestrationService).handleDocumentGenerationCallback(
            incomingRequest, AUTH_TOKEN, "FL-DIV-GNO-ENG-00059.docx", "caseListForPronouncement", "caseListForPronouncement");
        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(TEST_INCOMING_CASE_DETAILS.getCaseData()));
    }

    @Test
    public void whenGenerateDocument_whenUpdateBulkCaseHearingDetails() throws Exception {
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();

        when(caseOrchestrationService.handleDocumentGenerationCallback(eq(incomingRequest), eq(AUTH_TOKEN), any(), any(), any()))
            .thenReturn(TEST_INCOMING_CASE_DETAILS.getCaseData());

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.updateBulkCaseHearingDetails(AUTH_TOKEN, incomingRequest);

        verify(caseOrchestrationService).handleDocumentGenerationCallback(
            incomingRequest, AUTH_TOKEN, "FL-DIV-GNO-ENG-00020.docx", "coe", "certificateOfEntitlement");
        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), is(TEST_INCOMING_CASE_DETAILS.getCaseData()));
    }

    @Test
    public void givenWorkflowException_whenGenerateDocuments_thenReturnErrors() throws CaseOrchestrationServiceException {
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();
        CaseOrchestrationServiceException expectedException = new CaseOrchestrationServiceException("foo");
        when(caseOrchestrationService.handleDocumentGenerationCallback(eq(incomingRequest), eq(AUTH_TOKEN), any(), any(), any()))
            .thenThrow(expectedException);

        CaseOrchestrationServiceException actualException = assertThrows(CaseOrchestrationServiceException.class,
            () -> classUnderTest.generateDocument(AUTH_TOKEN, "a", "b", "c", incomingRequest));
        assertThat(actualException, is(expectedException));
    }

    @Test
    public void givenWorkflowException_whenPreparingToPrintForPronouncement_thenReturnErrors() throws CaseOrchestrationServiceException {
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();
        CaseOrchestrationServiceException expectedException = new CaseOrchestrationServiceException("foo");
        when(caseOrchestrationService.handleDocumentGenerationCallback(eq(incomingRequest), eq(AUTH_TOKEN), any(), any(), any()))
            .thenThrow(expectedException);

        CaseOrchestrationServiceException actualException = assertThrows(CaseOrchestrationServiceException.class,
            () -> classUnderTest.prepareToPrintForPronouncement(AUTH_TOKEN, incomingRequest));
        assertThat(actualException, is(expectedException));
    }

    @Test
    public void givenWorkflowException_whenUpdateBulkCaseHearingDetails_thenReturnErrors() throws CaseOrchestrationServiceException {
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();
        CaseOrchestrationServiceException expectedException = new CaseOrchestrationServiceException("foo");
        when(caseOrchestrationService.handleDocumentGenerationCallback(eq(incomingRequest), eq(AUTH_TOKEN), any(), any(), any()))
            .thenThrow(expectedException);

        CaseOrchestrationServiceException actualException = assertThrows(CaseOrchestrationServiceException.class,
            () -> classUnderTest.updateBulkCaseHearingDetails(AUTH_TOKEN, incomingRequest));
        assertThat(actualException, is(expectedException));
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
    public void testServiceMethodIsCalled_WhenHandleDaGrantedCallback() throws WorkflowException {
        when(caseOrchestrationService.handleDaGranted(any(), anyString())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        CcdCallbackRequest callbackRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.handleDaGranted(AUTH_TOKEN, callbackRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), equalTo(TEST_PAYLOAD_TO_RETURN));
        assertThat(response.getBody().getErrors(), is(not(hasSize(greaterThan(0)))));
        verify(caseOrchestrationService).handleDaGranted(callbackRequest, AUTH_TOKEN);
    }

    @Test
    public void testServiceMethodReturnsErros_IfWorkflowExceptionIsThrown() throws WorkflowException {
        when(caseOrchestrationService.handleDaGranted(any(), anyString())).thenThrow(new WorkflowException("This is an error."));

        CcdCallbackRequest callbackRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.handleDaGranted(AUTH_TOKEN, callbackRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getErrors(), hasItem("This is an error."));
        verify(caseOrchestrationService).handleDaGranted(callbackRequest, AUTH_TOKEN);
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

        when(caseOrchestrationService.processAosSolicitorNominated(incomingRequest, AUTH_TOKEN)).thenReturn(incomingPayload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.aosSolicitorNominated(AUTH_TOKEN, incomingRequest);

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
        when(caseOrchestrationService.processAosSolicitorNominated(incomingRequest, AUTH_TOKEN))
            .thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.aosSolicitorNominated(AUTH_TOKEN, incomingRequest);

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
        final List<String> expectedError = singletonList("Some error");
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

    @Test
    public void testDnAboutToBeGrantedCallsRightServiceMethod() throws CaseOrchestrationServiceException {
        when(caseOrchestrationService.processCaseBeforeDecreeNisiIsGranted(any(), eq(AUTH_TOKEN)))
            .thenReturn(singletonMap("newKey", "newValue"));

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(singletonMap("testKey", "testValue"))
                .build())
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.dnAboutToBeGranted(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), hasEntry("newKey", "newValue"));
        assertThat(response.getBody().getErrors(), is(nullValue()));
        verify(caseOrchestrationService).processCaseBeforeDecreeNisiIsGranted(eq(ccdCallbackRequest), eq(AUTH_TOKEN));
    }

    @Test
    public void testDnAboutToBeGrantedHandlesServiceException() throws CaseOrchestrationServiceException {
        when(caseOrchestrationService.processCaseBeforeDecreeNisiIsGranted(any(), eq(AUTH_TOKEN)))
            .thenThrow(new CaseOrchestrationServiceException(new Exception("This is a test error message.")));

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(singletonMap("testKey", "testValue"))
                .build())
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.dnAboutToBeGranted(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), is(nullValue()));
        assertThat(response.getBody().getErrors(), hasItem(equalTo("This is a test error message.")));
        verify(caseOrchestrationService).processCaseBeforeDecreeNisiIsGranted(eq(ccdCallbackRequest), eq(AUTH_TOKEN));
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
    public void testDnDecisionMadeCallsRightServiceMethodToNotifyAndClearState() throws WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(singletonMap("testKey", "testValue"))
                .build())
            .build();

        when(caseOrchestrationService.notifyForRefusalOrder(ccdCallbackRequest)).thenReturn(Collections.emptyMap());
        when(caseOrchestrationService.cleanStateCallback(ccdCallbackRequest, AUTH_TOKEN))
            .thenReturn(singletonMap("newKey", "newValue"));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.dnDecisionMadeCallback(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), hasEntry("newKey", "newValue"));
        assertThat(response.getBody().getErrors(), is(nullValue()));
        verify(caseOrchestrationService).notifyForRefusalOrder(eq(ccdCallbackRequest));
        verify(caseOrchestrationService).cleanStateCallback(eq(ccdCallbackRequest), eq(AUTH_TOKEN));
    }

    @Test
    public void testDnDecisionMadeReturnsWithoutMaking() throws WorkflowException {

        HashMap<String, Object> inputPayload = new HashMap<>();
        inputPayload.put(OrchestrationConstants.REFUSAL_REJECTION_ADDITIONAL_INFO_WELSH, EMPTY_STRING);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(inputPayload)
                .state(CcdStates.WELSH_LA_DECISION)
                .build())
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.dnDecisionMadeCallback(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), hasEntry(OrchestrationConstants.REFUSAL_REJECTION_ADDITIONAL_INFO_WELSH, EMPTY_STRING));
        assertThat(response.getBody().getErrors(), is(nullValue()));

        verify(caseOrchestrationService, never()).notifyForRefusalOrder(ccdCallbackRequest);
        verify(caseOrchestrationService, never()).cleanStateCallback(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void testServiceMethodIsCalled_WhenFlagCaseAsEligibleForDecreeAbsoluteForPetitioner() throws CaseOrchestrationServiceException {
        when(caseOrchestrationService.processApplicantDecreeAbsoluteEligibility(any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        CcdCallbackRequest callbackRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processApplicantDecreeAbsoluteEligibility(callbackRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(caseOrchestrationService).processApplicantDecreeAbsoluteEligibility(callbackRequest);
    }

    @Test
    public void testServiceMethodIsCalled_WhenNotifyPetitionerCanFinaliseDivorce() throws WorkflowException {
        when(caseOrchestrationService.handleMakeCaseEligibleForDaSubmitted(any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        CcdCallbackRequest callbackRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.handleMakeCaseEligibleForDaSubmitted(callbackRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getData(), equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(caseOrchestrationService).handleMakeCaseEligibleForDaSubmitted(callbackRequest);
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
    public void whenGetPetitionIssueFees_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        final CcdCallbackResponse ccdCallbackResponse = new CcdCallbackResponse();
        ccdCallbackResponse.setData(caseDetails.getCaseData());

        when(caseOrchestrationService.setOrderSummaryAssignRole(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(ccdCallbackResponse);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.getPetitionIssueFees(AUTH_TOKEN, ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void testRemoveCaseLink_callServiceMethod() throws WorkflowException {
        CcdCallbackRequest request = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails
                .builder()
                .caseId(TEST_CASE_ID)
                .build()).build();
        when(caseOrchestrationService.removeBulkLink(request)).thenReturn(TEST_PAYLOAD_TO_RETURN);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.removeBulkLinkFromCase(request);
        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().data(TEST_PAYLOAD_TO_RETURN).build()));
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
    public void testIssueAosOffline_ForRespondent_callsRightService() throws CaseOrchestrationServiceException {
        when(aosService.issueAosPackOffline(any(), any(), any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.issueAosPackOffline(AUTH_TOKEN, ccdCallbackRequest, RESPONDENT);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(aosService).issueAosPackOffline(eq(AUTH_TOKEN), eq(caseDetails), eq(RESPONDENT));
    }

    @Test
    public void testIssueAosOffline_ForCoRespondent_callsRightService() throws CaseOrchestrationServiceException {
        when(aosService.issueAosPackOffline(any(), any(), any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.issueAosPackOffline(AUTH_TOKEN, ccdCallbackRequest, CO_RESPONDENT);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(aosService).issueAosPackOffline(eq(AUTH_TOKEN), eq(caseDetails), eq(CO_RESPONDENT));
    }

    @Test
    public void testIssueAosOffline_returnsErrors_whenServiceThrowsException() throws CaseOrchestrationServiceException {
        when(aosService.issueAosPackOffline(any(), any(), any()))
            .thenThrow(new CaseOrchestrationServiceException(new RuntimeException("Error message")));

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.issueAosPackOffline(AUTH_TOKEN, ccdCallbackRequest, RESPONDENT);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getErrors(), hasItem(equalTo("Error message")));
        assertThat(response.getBody().getData(), is(nullValue()));
    }

    @Test
    public void testProcessAosOfflineAnswers_callsRightService() throws CaseOrchestrationServiceException {
        when(aosService.processAosPackOfflineAnswers(any(), any(), any())).thenReturn(TEST_PAYLOAD_TO_RETURN);
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(TEST_INCOMING_CASE_DETAILS).build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processAosPackOfflineAnswers(AUTH_TOKEN, ccdCallbackRequest, RESPONDENT);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(aosService).processAosPackOfflineAnswers(eq(AUTH_TOKEN), eq(TEST_INCOMING_CASE_DETAILS), eq(RESPONDENT));
    }

    @Test
    public void testProcessAosOfflineAnswers_returnsErrors_whenServiceThrowsException() throws CaseOrchestrationServiceException {
        when(aosService.processAosPackOfflineAnswers(any(), any(), any()))
            .thenThrow(new CaseOrchestrationServiceException(new RuntimeException("Error message")));

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.processAosPackOfflineAnswers(AUTH_TOKEN, ccdCallbackRequest, RESPONDENT);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getErrors(), hasItem(equalTo("Error message")));
        assertThat(response.getBody().getData(), is(nullValue()));
    }

    @Test
    public void testRemoveFromCallbackListed_ForCoRespondent_callsRightService() throws WorkflowException, JsonProcessingException {
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        when(caseOrchestrationService.removeBulkListed(ccdCallbackRequest)).thenReturn(TEST_PAYLOAD_TO_RETURN);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.removeBulkLinkFromCaseListed(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), equalTo(TEST_PAYLOAD_TO_RETURN));
    }

    @Test
    public void testRemoveCaseOnDigitalDecreeNisi_returnsPayload_whenExecuted() throws WorkflowException {
        Map<String, Object> caseData = Collections.singletonMap(DN_OUTCOME_FLAG_CCD_FIELD, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.removeDnOutcomeCaseFlag(ccdCallbackRequest)).thenReturn(Collections.emptyMap());

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.removeDnOutcomeCaseFlag(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), is(Collections.emptyMap()));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testRemoveLegalAdvisorMakeDecisionFields_returnsPayload_whenExecuted() throws WorkflowException {
        Map<String, Object> caseData = Collections.singletonMap(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.removeDnOutcomeCaseFlag(ccdCallbackRequest)).thenReturn(Collections.emptyMap());

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.removeDnOutcomeCaseFlag(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), is(Collections.emptyMap()));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testRemoveDNGrantedDocuments_returnsPayload_whenExecuted() throws WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(DUMMY_CASE_DATA).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.removeDNGrantedDocuments(ccdCallbackRequest)).thenReturn(DUMMY_CASE_DATA);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.removeDNGrantedDocuments(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), is(DUMMY_CASE_DATA));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testRemoveDNGrantedDocumentsException_returnsError_whenExecuted() throws WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(DUMMY_CASE_DATA).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.removeDNGrantedDocuments(ccdCallbackRequest)).thenThrow(new WorkflowException("Workflow error"));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.removeDNGrantedDocuments(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getErrors(), contains("Workflow error"));
    }

    @Test
    public void testDecreeNisiDecisionState_returnsPayload_whenExecuted() throws WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(DUMMY_CASE_DATA).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.decreeNisiDecisionState(ccdCallbackRequest)).thenReturn(DUMMY_CASE_DATA);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.decreeNisiDecisionState(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), is(DUMMY_CASE_DATA));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testDecreeNisiDecisionStateException_returnsError_whenExecuted() throws WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(DUMMY_CASE_DATA).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.decreeNisiDecisionState(ccdCallbackRequest)).thenThrow(new WorkflowException("Workflow error"));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.decreeNisiDecisionState(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getErrors(), contains("Workflow error"));
    }

    @Test
    public void testSendAmendApplicationEmailException_returnsError_whenExecuted() throws WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(DUMMY_CASE_DATA).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        String errorMessage = "Workflow error";
        when(caseOrchestrationService
            .sendAmendApplicationEmail(ccdCallbackRequest)).thenThrow(new WorkflowException(errorMessage));

        WorkflowException workflowException = assertThrows(WorkflowException.class, () -> classUnderTest.amendApplication(ccdCallbackRequest));

        assertThat(workflowException.getMessage(), is(errorMessage));
    }

    @Test
    public void testSendAmendApplicationEmail_returnsPayload_whenExecuted() throws WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(DUMMY_CASE_DATA).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.sendAmendApplicationEmail(ccdCallbackRequest)).thenReturn(DUMMY_CASE_DATA);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.amendApplication(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), is(DUMMY_CASE_DATA));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testSendClarification_thenExecuteWorkflow() throws WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(DUMMY_CASE_DATA).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.sendClarificationSubmittedNotificationEmail(ccdCallbackRequest))
            .thenReturn(CcdCallbackResponse
                .builder()
                .data(DUMMY_CASE_DATA)
                .build());

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.clarificationSubmitted(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), is(DUMMY_CASE_DATA));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testDefault_Language_Welsh_No() throws WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(new HashMap<>()).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.defaultValue(TEST_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData().get(LANGUAGE_PREFERENCE_WELSH), is(NO_VALUE));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testDefault_Language_Welsh_Yes() throws WorkflowException {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.defaultValue(TEST_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData().get(LANGUAGE_PREFERENCE_WELSH), is(YES_VALUE));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testWelshContinue() throws WorkflowException {

        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.welshContinue(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testWelshSetPreviousState() throws WorkflowException {

        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.welshSetPreviousState(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
    }

    @Test
    public void testWelshContinueIntercept() throws WorkflowException {

        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.welshContinueIntercept(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
    }

    @Test
    public void testMakeServiceDecisionStateChange() throws ServiceJourneyServiceException {
        when(serviceJourneyService.makeServiceDecision(any(), anyString()))
            .thenReturn(CcdCallbackResponse.builder().build());

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .state(AWAITING_DECREE_NISI)
                .build())
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.makeServiceDecision(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testServiceDecisionMade() throws CaseOrchestrationServiceException {
        when(serviceJourneyService.serviceDecisionMade(any(), anyString()))
            .thenReturn(CcdCallbackResponse.builder().build());

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .state(AWAITING_SERVICE_CONSIDERATION)
                .build())
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.serviceDecisionMade(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testGenerateGeneralOrder() throws CaseOrchestrationServiceException {
        when(generalOrderService.generateGeneralOrder(any(), anyString()))
            .thenReturn(CaseDetails.builder().build());

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().build())
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateGeneralOrder(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void testGenerateDraftOfGeneralOrder() throws CaseOrchestrationServiceException {
        when(generalOrderService.generateGeneralOrderDraft(any(), anyString()))
            .thenReturn(CaseDetails.builder().build());

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().build())
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateDraftOfGeneralOrder(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void shouldReturnErrorMessageInResponse_whenCaseOrchestrationServiceExceptionIsThrown() {
        CaseOrchestrationServiceException serviceException = spy(new CaseOrchestrationServiceException("This is a test error message"));
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.handleCaseOrchestrationServiceExceptionForCcdCallback(serviceException);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        CcdCallbackResponse ccdCallbackResponse = response.getBody();
        assertThat(ccdCallbackResponse, is(notNullValue()));
        assertThat(ccdCallbackResponse.getData(), is(nullValue()));
        assertThat(ccdCallbackResponse.getErrors(), hasSize(1));
        assertThat(ccdCallbackResponse.getErrors(), hasItem("This is a test error message"));
        verify(serviceException).getIdentifiableMessage();
    }

    @Test
    public void shouldReturnOK_SetupServicePaymentEventIsCalled() throws CaseOrchestrationServiceException {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        final CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        when(serviceJourneyService.setupConfirmServicePaymentEvent(caseDetails)).thenReturn(caseData);

        final ResponseEntity<CcdCallbackResponse> response = classUnderTest
            .setupConfirmServicePaymentEvent(ccdCallbackRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(expectedResponse));
    }

    @Test
    public void shouldReturnOK_SetupGeneralReferralPaymentEventIsCalled() throws CaseOrchestrationServiceException {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        final CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        when(generalReferralService.setupGeneralReferralPaymentEvent(caseDetails)).thenReturn(caseData);

        final ResponseEntity<CcdCallbackResponse> response = classUnderTest
            .setupGeneralReferralPaymentEvent(ccdCallbackRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(expectedResponse));
    }

    @Test
    public void shouldReturnOK_WhenGeneralReferralPaymentEventIsCalled() throws CaseOrchestrationServiceException {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        final CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        when(generalReferralService.generalReferralPaymentEvent(caseDetails)).thenReturn(caseData);

        final ResponseEntity<CcdCallbackResponse> response = classUnderTest.generalReferralPaymentEvent(ccdCallbackRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(expectedResponse));
    }


    @Test
    public void shouldReturnOK_WhenConfirmServicePaymentEventIsCalled() throws CaseOrchestrationServiceException {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        final CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        when(serviceJourneyService.confirmServicePaymentEvent(caseDetails)).thenReturn(caseData);

        final ResponseEntity<CcdCallbackResponse> response = classUnderTest.confirmServicePaymentEvent(ccdCallbackRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is(expectedResponse));
    }

    @Test
    public void shouldCallRightServiceMethod_ForPreparingAosNotReceivedForSubmission() throws CaseOrchestrationServiceException {
        when(aosService.prepareAosNotReceivedEventForSubmission(any(), any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(TEST_INCOMING_CASE_DETAILS).build();
        ResponseEntity<CcdCallbackResponse> ccdCallbackResponse = classUnderTest.prepareAosNotReceivedForSubmission(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getBody().getData(), equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(aosService).prepareAosNotReceivedEventForSubmission(AUTH_TOKEN, TEST_INCOMING_CASE_DETAILS);
    }

    @Test
    public void shouldCallRightServiceMethod_forTriggeringGeneralEmail() throws CaseOrchestrationServiceException {
        when(generalEmailService.createGeneralEmail(any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(TEST_INCOMING_CASE_DETAILS).build();
        ResponseEntity<CcdCallbackResponse> ccdCallbackResponse = classUnderTest.createGeneralEmail(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getBody().getData(), equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(generalEmailService).createGeneralEmail(TEST_INCOMING_CASE_DETAILS);
    }

    @Test
    public void shouldReturnOk_whenGeneralReferralServiceIsCalled() throws CaseOrchestrationServiceException {
        when(generalReferralService.receiveReferral(any())).thenReturn(CcdCallbackResponse.builder().build());

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().build())
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generalReferral(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        verify(generalReferralService).receiveReferral(any(CcdCallbackRequest.class));
    }

    @Test
    public void shouldReturnOk_whenGeneralConsiderationIsCalled() throws CaseOrchestrationServiceException {
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(generalReferralService.generalConsideration(caseDetails))
            .thenReturn(new HashMap<>());

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generalConsideration(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        verify(generalReferralService).generalConsideration(caseDetails);
    }

    @Test
    public void shouldReturnOk_whenConfirmAlternativeServiceIsCalled() throws CaseOrchestrationServiceException {
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(alternativeServiceService.confirmAlternativeService(caseDetails))
            .thenReturn(CaseDetails.builder().build());

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.confirmAlternativeService(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        verify(alternativeServiceService).confirmAlternativeService(caseDetails);
    }

    @Test
    public void shouldReturnOk_whenReturnToStateBeforeGeneralReferralIsCalled() throws CaseOrchestrationServiceException {
        Map<String, Object> caseData = ImmutableMap.of(
            CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, "previousCaseState");
        CcdCallbackRequest ccdCallbackRequest = ccdRequestWithData(caseData);
        when(generalReferralService.returnToStateBeforeGeneralReferral(any()))
            .thenReturn(CcdCallbackResponse.builder()
                .state("previousCaseState")
                .data(caseData)
                .build());

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.returnToStateBeforeGeneralReferral(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getState(), is("previousCaseState"));
        verify(generalReferralService).returnToStateBeforeGeneralReferral(ccdCallbackRequest.getCaseDetails());
    }

    public void shouldReturnOk_whenConfirmProcessServerServiceIsCalled() throws CaseOrchestrationServiceException {
        when(alternativeServiceService.confirmProcessServerService(TEST_INCOMING_CASE_DETAILS))
            .thenReturn(CaseDetails.builder().caseData(TEST_PAYLOAD_TO_RETURN)
                .build());

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(TEST_INCOMING_CASE_DETAILS)
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.confirmProcessServerService(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        assertThat(response.getBody().getData(), is(TEST_PAYLOAD_TO_RETURN));
        verify(alternativeServiceService).confirmProcessServerService(TEST_INCOMING_CASE_DETAILS);
    }

    @Test
    public void shouldReturnOk_whenAosNotReceivedForProcessServerIsCalled() throws CaseOrchestrationServiceException {
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(alternativeServiceService.aosNotReceivedForProcessServer(caseDetails))
            .thenReturn(CaseDetails.builder().build());

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.aosNotReceivedForProcessServer(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        verify(alternativeServiceService).aosNotReceivedForProcessServer(caseDetails);
    }

    @Test
    public void shouldReturnOk_whenAlternativeServiceConfirmedIsCalled() throws CaseOrchestrationServiceException {
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(alternativeServiceService.alternativeServiceConfirmed(caseDetails))
            .thenReturn(CaseDetails.builder().build());

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.alternativeServiceConfirmed(ccdCallbackRequest);

        assertThat(response.getStatusCode(), equalTo(OK));
        verify(alternativeServiceService).alternativeServiceConfirmed(caseDetails);
    }

    @Test
    public void shouldCallAdequateServiceWhenUpdatingCourtOrderDocuments() throws CaseOrchestrationServiceException {
        when(courtOrderDocumentsUpdateService.updateExistingCourtOrderDocuments(AUTH_TOKEN, TEST_INCOMING_CASE_DETAILS))
            .thenReturn(TEST_PAYLOAD_TO_RETURN);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(TEST_INCOMING_CASE_DETAILS).build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.updateCourtOrderDocuments(AUTH_TOKEN, ccdCallbackRequest);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody().getErrors(), is(nullValue()));
        assertThat(response.getBody().getData(), is(TEST_PAYLOAD_TO_RETURN));
        verify(courtOrderDocumentsUpdateService).updateExistingCourtOrderDocuments(AUTH_TOKEN, TEST_INCOMING_CASE_DETAILS);
    }

}