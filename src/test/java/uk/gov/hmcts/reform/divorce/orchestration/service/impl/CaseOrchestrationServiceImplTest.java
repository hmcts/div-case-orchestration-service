package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Fee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Payment;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AuthenticateRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DNSubmittedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DeleteDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GetCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ProcessPbaPaymentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SaveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerGenericEmailNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendRespondentSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SetOrderSummaryWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorCreateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDnCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;


@RunWith(MockitoJUnitRunner.class)
public class CaseOrchestrationServiceImplTest {

    @Mock
    private CcdCallbackWorkflow ccdCallbackWorkflow;

    @Mock
    private RetrieveDraftWorkflow retrieveDraftWorkflow;

    @Mock
    private SaveDraftWorkflow saveDraftWorkflow;

    @Mock
    private DeleteDraftWorkflow deleteDraftWorkflow;

    @Mock
    private AuthenticateRespondentWorkflow authenticateRespondentWorkflow;

    @Mock
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    @Mock
    private UpdateToCCDWorkflow updateToCCDWorkflow;

    @Mock
    private LinkRespondentWorkflow linkRespondentWorkflow;

    @Mock
    private SendPetitionerSubmissionNotificationWorkflow sendPetitionerSubmissionNotificationWorkflow;

    @Mock
    private SendPetitionerGenericEmailNotificationWorkflow sendPetitionerGenericEmailNotificationWorkflow;

    @Mock
    private SendRespondentSubmissionNotificationWorkflow sendRespondentSubmissionNotificationWorkflow;

    @Mock
    private SetOrderSummaryWorkflow setOrderSummaryWorkflow;

    @Mock
    private ProcessPbaPaymentWorkflow processPbaPaymentWorkflow;

    @Mock
    private SolicitorCreateWorkflow solicitorCreateWorkflow;

    @Mock
    private SubmitAosCaseWorkflow submitAosCaseWorkflow;

    @Mock
    private DNSubmittedWorkflow dnSubmittedWorkflow;

    @Mock
    private SubmitDnCaseWorkflow submitDnCaseWorkflow;

    @Mock
    private GetCaseWorkflow getCaseWorkflow;

    @Mock
    private RetrieveAosCaseWorkflow retrieveAosCaseWorkflow;

    @InjectMocks
    private CaseOrchestrationServiceImpl classUnderTest;

    @Mock
    private AuthUtil authUtil;

    private CreateEvent createEventRequest;

    private Map<String, Object> requestPayload;

    private Map<String, Object> expectedPayload;

    @Before
    public void setUp() {
        requestPayload = Collections.emptyMap();
        createEventRequest = CreateEvent.builder()
                .caseDetails(
                        CaseDetails.builder()
                                .caseData(requestPayload)
                                .caseId(TEST_CASE_ID)
                                .state(TEST_STATE)
                                .build())
                .eventId(TEST_EVENT_ID)
                .token(TEST_TOKEN)
                .build();
        expectedPayload = Collections.singletonMap(PIN, TEST_PIN);
    }

    @Test
    public void givenGenerateInvitationIsTrue_whenCcdCallbackHandler_thenReturnExpected()
            throws WorkflowException {
        //given
        when(ccdCallbackWorkflow.run(createEventRequest, AUTH_TOKEN, true)).thenReturn(expectedPayload);

        //when
        Map<String, Object> actual = classUnderTest.ccdCallbackHandler(createEventRequest, AUTH_TOKEN, true);

        //then
        assertEquals(expectedPayload, actual);
        assertEquals(expectedPayload.get(PIN), TEST_PIN);
    }

    @Test
    public void givenGenerateInvitationIsFalse_whenCcdCallbackHandler_thenReturnExpected()
        throws WorkflowException {
        //given
        when(ccdCallbackWorkflow.run(createEventRequest, AUTH_TOKEN, false)).thenReturn(expectedPayload);

        //when
        Map<String, Object> actual = classUnderTest.ccdCallbackHandler(createEventRequest, AUTH_TOKEN, false);

        //then
        assertEquals(expectedPayload, actual);
        assertEquals(expectedPayload.get(PIN), TEST_PIN);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenDraftInWorkflowResponse_whenGetDraft_thenReturnPayloadFromWorkflow() throws WorkflowException {
        Map<String, Object> testExpectedPayload = mock(Map.class);

        when(retrieveDraftWorkflow.run(AUTH_TOKEN, true)).thenReturn(testExpectedPayload);
        assertEquals(testExpectedPayload,classUnderTest.getDraft(AUTH_TOKEN, true));
    }

    @Test
    public void whenRetrieveAosCase_thenProceedAsExpected() throws WorkflowException {
        final CaseDataResponse caseDataResponse = CaseDataResponse.builder().build();

        when(retrieveAosCaseWorkflow.run(true, AUTH_TOKEN)).thenReturn(caseDataResponse);

        assertEquals(caseDataResponse, classUnderTest.retrieveAosCase(true, AUTH_TOKEN));

        verify(retrieveAosCaseWorkflow).run(true, AUTH_TOKEN);
    }

    @Test
    public void whenGetCase_thenProceedAsExpected() throws WorkflowException {
        final CaseDataResponse caseDataResponse = CaseDataResponse.builder().build();

        when(getCaseWorkflow.run(AUTH_TOKEN)).thenReturn(caseDataResponse);

        assertEquals(caseDataResponse, classUnderTest.getCase(AUTH_TOKEN));

        verify(getCaseWorkflow).run(AUTH_TOKEN);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenSaveDraft_thenReturnPayloadFromWorkflow() throws WorkflowException {
        Map<String, Object> payload = mock(Map.class);
        Map<String, Object> testExpectedPayload = mock(Map.class);

        when(saveDraftWorkflow.run(payload,AUTH_TOKEN, TEST_USER_EMAIL)).thenReturn(testExpectedPayload);
        assertEquals(testExpectedPayload,classUnderTest.saveDraft(payload, AUTH_TOKEN, TEST_USER_EMAIL));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenErrorOnDraftWorkflow_whenSaveDraft_thenReturnErrors() throws WorkflowException {
        Map<String, Object> expectedErrors = mock(Map.class);
        Map<String, Object> payload = mock(Map.class);
        Map<String, Object> workflowResponsePayload = mock(Map.class);

        when(saveDraftWorkflow.run(payload,AUTH_TOKEN, TEST_USER_EMAIL)).thenReturn(workflowResponsePayload);
        when(saveDraftWorkflow.errors()).thenReturn(expectedErrors);

        assertEquals(expectedErrors,classUnderTest.saveDraft(payload, AUTH_TOKEN, TEST_USER_EMAIL));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenUserWithADraft_whenDeleteDraft_thenReturnPayloadFromWorkflow() throws WorkflowException {
        Map<String, Object> testExpectedPayload = mock(Map.class);
        when(deleteDraftWorkflow.run(AUTH_TOKEN)).thenReturn(testExpectedPayload);
        assertEquals(testExpectedPayload,classUnderTest.deleteDraft(AUTH_TOKEN));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenErrorOnDraftWorkflow_whenDeleteDraft_thenReturnErrors() throws WorkflowException {
        Map<String, Object> expectedErrors = mock(Map.class);
        Map<String, Object> workflowResponsePayload = mock(Map.class);

        when(deleteDraftWorkflow.run(AUTH_TOKEN)).thenReturn(workflowResponsePayload);
        when(deleteDraftWorkflow.errors()).thenReturn(expectedErrors);

        assertEquals(expectedErrors, classUnderTest.deleteDraft(AUTH_TOKEN));
    }


    @SuppressWarnings("ConstantConditions")
    @Test
    public void whenAuthenticateRespondent_thenProceedAsExpected() throws WorkflowException {
        final Boolean expected = true;

        //given
        when(authenticateRespondentWorkflow.run(AUTH_TOKEN)).thenReturn(expected);

        //when
        Boolean actual = classUnderTest.authenticateRespondent(AUTH_TOKEN);

        //then
        assertEquals(expected, actual);

        verify(authenticateRespondentWorkflow).run(AUTH_TOKEN);
    }

    @Test
    public void givenCaseDataValid_whenSubmit_thenReturnPayload() throws Exception {
        // given
        when(submitToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        when(submitToCCDWorkflow.errors()).thenReturn(Collections.emptyMap());

        // when
        Map<String, Object> actual = classUnderTest.submit(requestPayload, AUTH_TOKEN);

        // then
        assertEquals(expectedPayload, actual);

        verify(submitToCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(submitToCCDWorkflow).errors();
    }

    @Test
    public void givenCaseDataInvalid_whenSubmit_thenReturnListOfErrors() throws Exception {
        // given
        when(submitToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        Map<String, Object> errors = Collections.singletonMap("new_Error", "An Error");
        when(submitToCCDWorkflow.errors()).thenReturn(errors);

        // when
        Map<String, Object> actual = classUnderTest.submit(requestPayload, AUTH_TOKEN);

        // then
        assertEquals(errors, actual);

        verify(submitToCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(submitToCCDWorkflow, times(2)).errors();
    }

    @Test
    public void givenCaseDataValid_whenUpdate_thenReturnPayload() throws Exception {
        // given
        when(updateToCCDWorkflow.run(requestPayload, AUTH_TOKEN, TEST_CASE_ID))
                .thenReturn(requestPayload);

        // when
        Map<String, Object> actual = classUnderTest.update(requestPayload, AUTH_TOKEN, TEST_CASE_ID);

        // then
        assertEquals(requestPayload, actual);

        verify(updateToCCDWorkflow).run(requestPayload, AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void givenCaseDataValid_whenPaymentUpdate_thenReturnPayload() throws Exception {
        PaymentUpdate paymentUpdate  = new PaymentUpdate();
        paymentUpdate.setCcdCaseNumber("1232132");
        paymentUpdate.setStatus("success");
        paymentUpdate.setAmount(new BigDecimal(550.00));
        Fee fee = new Fee();
        fee.setCode("X243");
        paymentUpdate.setFees(Arrays.asList(fee, fee));
        paymentUpdate.setChannel("online");

        // given
        when(updateToCCDWorkflow.run(any(), any(), any()))
            .thenReturn(requestPayload);

        when(authUtil.getCaseworkerToken()).thenReturn("testtoken");

        // when
        Map<String, Object> actual = classUnderTest.update(paymentUpdate);

        // then
        assertEquals(requestPayload, actual);


        Payment payment = Payment.builder().build();
        payment.setPaymentFeeId("X243");
        payment.setPaymentChannel("online");
        payment.setPaymentStatus("success");
        payment.setPaymentAmount("55000");
        final Map<String, Object> updateEvent = new HashMap<>();
        updateEvent.put("eventData", Collections.singletonMap("payment", payment));
        updateEvent.put("eventId", "paymentMade");

        verify(updateToCCDWorkflow).run(updateEvent, "testtoken", "1232132");
    }

    @Test
    public void whenLinkRespondent_thenProceedAsExpected() throws WorkflowException {
        final UserDetails userDetails = UserDetails.builder().build();

        when(linkRespondentWorkflow.run(AUTH_TOKEN, TEST_CASE_ID, TEST_PIN)).thenReturn(userDetails);

        assertEquals(userDetails, classUnderTest.linkRespondent(AUTH_TOKEN, TEST_CASE_ID, TEST_PIN));

        verify(linkRespondentWorkflow).run(AUTH_TOKEN, TEST_CASE_ID, TEST_PIN);
    }

    @Test
    public void givenCaseData_whenSendPetitionerSubmissionNotification_thenReturnPayload() throws Exception {
        // given
        when(sendPetitionerSubmissionNotificationWorkflow.run(createEventRequest))
                .thenReturn(requestPayload);

        // when
        Map<String, Object> actual = classUnderTest.sendPetitionerSubmissionNotificationEmail(createEventRequest);

        // then
        assertEquals(requestPayload, actual);

        verify(sendPetitionerSubmissionNotificationWorkflow).run(createEventRequest);
    }

    @Test
    public void givenCaseData_whenSendPetitionerGenericEmailNotification_thenReturnPayload() throws Exception {
        // given
        when(sendPetitionerGenericEmailNotificationWorkflow.run(createEventRequest))
                .thenReturn(requestPayload);
        // when
        Map<String, Object> actual = classUnderTest.sendPetitionerGenericUpdateNotificationEmail(createEventRequest);
        // then
        assertEquals(requestPayload, actual);
        verify(sendPetitionerGenericEmailNotificationWorkflow).run(createEventRequest);
    }

    @Test
    public void givenCaseData_whenSendRespondentSubmissionNotification_thenReturnPayload() throws Exception {
        when(sendRespondentSubmissionNotificationWorkflow.run(createEventRequest)).thenReturn(requestPayload);

        Map<String, Object> returnedPayload = classUnderTest
                .sendRespondentSubmissionNotificationEmail(createEventRequest);

        assertEquals(requestPayload, returnedPayload);
        verify(sendRespondentSubmissionNotificationWorkflow).run(createEventRequest);
    }

    @Test
    public void givenCaseData_whenSetOrderSummary_thenReturnPayload() throws Exception {
        // given
        when(setOrderSummaryWorkflow.run(requestPayload))
                .thenReturn(requestPayload);

        // when
        Map<String, Object> actual = classUnderTest.setOrderSummary(createEventRequest);

        // then
        assertEquals(requestPayload, actual);

        verify(setOrderSummaryWorkflow).run(requestPayload);
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_thenReturnPayload() throws Exception {
        // given
        when(processPbaPaymentWorkflow.run(createEventRequest, AUTH_TOKEN))
                .thenReturn(requestPayload);

        // when
        Map<String, Object> actual = classUnderTest.processPbaPayment(createEventRequest, AUTH_TOKEN);

        // then
        assertEquals(requestPayload, actual);

        verify(processPbaPaymentWorkflow).run(createEventRequest, AUTH_TOKEN);
    }


    @Test
    public void givenCaseDataInvalid_whenProcessPbaPayment_thenReturnListOfErrors() throws Exception {
        // given
        when(processPbaPaymentWorkflow.run(createEventRequest, AUTH_TOKEN))
                .thenReturn(requestPayload);
        Map<String, Object> errors = Collections.singletonMap("new_Error", "An Error");
        when(processPbaPaymentWorkflow.errors()).thenReturn(errors);

        // when
        Map<String, Object> actual = classUnderTest.processPbaPayment(createEventRequest, AUTH_TOKEN);

        // then
        assertEquals(errors, actual);

        verify(processPbaPaymentWorkflow).run(createEventRequest, AUTH_TOKEN);
        verify(processPbaPaymentWorkflow, times(2)).errors();
    }

    @Test
    public void givenCaseData_whenSolicitorCreate_thenReturnPayload() throws Exception {
        // given
        when(solicitorCreateWorkflow.run(requestPayload))
                .thenReturn(requestPayload);

        // when
        Map<String, Object> actual = classUnderTest.solicitorCreate(createEventRequest);

        // then
        assertEquals(requestPayload, actual);

        verify(solicitorCreateWorkflow).run(requestPayload);
    }

    @Test
    public void givenCaseData_whenSubmitAosCase_thenReturnPayload() throws Exception {
        when(submitAosCaseWorkflow.run(requestPayload, AUTH_TOKEN, TEST_CASE_ID)).thenReturn(requestPayload);

        assertEquals(requestPayload, classUnderTest.submitAosCase(requestPayload, AUTH_TOKEN, TEST_CASE_ID));

        verify(submitAosCaseWorkflow).run(requestPayload, AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void givenDnCaseData_whenSubmitDnCase_thenReturnPayload() throws Exception {
        when(submitDnCaseWorkflow.run(requestPayload, AUTH_TOKEN, TEST_CASE_ID)).thenReturn(requestPayload);

        assertEquals(requestPayload, classUnderTest.submitDnCase(requestPayload, AUTH_TOKEN, TEST_CASE_ID));

        verify(submitDnCaseWorkflow).run(requestPayload, AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void givenNoError_whenExecuteDnSubmittedWorkflow_thenReturnCaseData() throws WorkflowException {
        CcdCallbackResponse expectedResponse =  CcdCallbackResponse.builder()
                .data(createEventRequest.getCaseDetails().getCaseData())
                .build();

        when(dnSubmittedWorkflow
                .run(createEventRequest, AUTH_TOKEN))
                .thenReturn(createEventRequest.getCaseDetails().getCaseData());

        CcdCallbackResponse ccdResponse = classUnderTest.dnSubmitted(createEventRequest, AUTH_TOKEN);

        assertEquals(expectedResponse, ccdResponse);
    }

    @Test
    public void givenError_whenExecuteDnSubmittedWorkflow_thenReturnErrorData() throws WorkflowException {

        Map<String, Object> workflowError = Collections.singletonMap("ErrorKey", "Error value");
        when(dnSubmittedWorkflow
                .run(createEventRequest, AUTH_TOKEN))
                .thenReturn(createEventRequest.getCaseDetails().getCaseData());

        when(dnSubmittedWorkflow.errors()).thenReturn(workflowError);

        CcdCallbackResponse expectedResponse =  CcdCallbackResponse.builder()
                .errors(Collections.singletonList("Error value"))
                .build();

        CcdCallbackResponse ccdResponse = classUnderTest.dnSubmitted(createEventRequest, AUTH_TOKEN);

        assertEquals(expectedResponse, ccdResponse);
    }


    @After
    public void tearDown() {
        createEventRequest = null;
        requestPayload = null;
        expectedPayload = null;
    }
}