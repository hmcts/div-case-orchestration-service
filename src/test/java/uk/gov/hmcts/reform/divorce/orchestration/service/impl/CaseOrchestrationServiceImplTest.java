package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Fee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Payment;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AmendPetitionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AuthenticateRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.BulkCaseUpdateDnPronounceDatesWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.BulkCaseUpdateHearingDetailsEventWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CaseLinkedForHearingWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DNSubmittedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DecreeNisiAboutToBeGrantedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DeleteDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GenerateCoRespondentAnswersWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GetCaseWithIdWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GetCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.IssueEventWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ProcessAwaitingPronouncementCasesWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ProcessPbaPaymentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RespondentSolicitorNominatedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SaveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendCoRespondSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerClarificationRequestNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerEmailNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendRespondentSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SeparationFieldsWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SetDNOutcomeFlagWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SetOrderSummaryWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorCreateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitCoRespondentAosWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDnCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitRespondentAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ValidateBulkCaseListingWorkflow;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.ALLOCATED_COURT_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CaseOrchestrationServiceImplTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private IssueEventWorkflow issueEventWorkflow;

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
    private SendPetitionerEmailNotificationWorkflow sendPetitionerEmailNotificationWorkflow;

    @Mock
    private SendPetitionerClarificationRequestNotificationWorkflow sendPetitionerClarificationRequestNotificationWorkflow;

    @Mock
    private SendRespondentSubmissionNotificationWorkflow sendRespondentSubmissionNotificationWorkflow;

    @Mock
    private SendCoRespondSubmissionNotificationWorkflow sendCoRespondSubmissionNotificationWorkflow;

    @Mock
    private SetOrderSummaryWorkflow setOrderSummaryWorkflow;

    @Mock
    private ProcessPbaPaymentWorkflow processPbaPaymentWorkflow;

    @Mock
    private SolicitorCreateWorkflow solicitorCreateWorkflow;

    @Mock
    private SubmitRespondentAosCaseWorkflow submitRespondentAosCaseWorkflow;

    @Mock
    private SubmitCoRespondentAosWorkflow submitCoRespondentAosWorkflow;

    @Mock
    private DNSubmittedWorkflow dnSubmittedWorkflow;

    @Mock
    private SubmitDnCaseWorkflow submitDnCaseWorkflow;

    @Mock
    private GetCaseWorkflow getCaseWorkflow;

    @Mock
    private RetrieveAosCaseWorkflow retrieveAosCaseWorkflow;

    @Mock
    private AmendPetitionWorkflow amendPetitionWorkflow;

    @Mock
    private CaseLinkedForHearingWorkflow caseLinkedForHearingWorkflow;

    @Mock
    private ProcessAwaitingPronouncementCasesWorkflow processAwaitingPronouncementCasesWorkflow;

    @Mock
    private GetCaseWithIdWorkflow getCaseWithIdWorkflow;

    @Mock
    private GenerateCoRespondentAnswersWorkflow generateCoRespondentAnswersWorkflow;

    @Mock
    private DocumentGenerationWorkflow documentGenerationWorkflow;

    @Mock
    private RespondentSolicitorNominatedWorkflow respondentSolicitorNominatedWorkflow;

    @Mock
    private BulkCaseUpdateHearingDetailsEventWorkflow bulkCaseUpdateHearingDetailsEventWorkflow;

    @Mock
    private SeparationFieldsWorkflow separationFieldsWorkflow;

    @Mock
    private ValidateBulkCaseListingWorkflow validateBulkCaseListingWorkflow;

    @Mock
    private DecreeNisiAboutToBeGrantedWorkflow decreeNisiAboutToBeGrantedWorkflow;

    @Mock
    private BulkCaseUpdateDnPronounceDatesWorkflow bulkCaseUpdateDnPronounceDatesWorkflow;

    @Mock
    private SetDNOutcomeFlagWorkflow setDNOutcomeFlagWorkflow;

    @InjectMocks
    private CaseOrchestrationServiceImpl classUnderTest;

    @Mock
    private AuthUtil authUtil;

    private CcdCallbackRequest ccdCallbackRequest;

    private Map<String, Object> requestPayload;

    private Map<String, Object> expectedPayload;

    @Before
    public void setUp() {
        requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");
        ccdCallbackRequest = CcdCallbackRequest.builder()
                .caseDetails(
                        CaseDetails.builder()
                                .caseData(requestPayload)
                                .caseId(TEST_CASE_ID)
                                .state(TEST_STATE)
                                .build())
                .eventId(TEST_EVENT_ID)
                .token(TEST_TOKEN)
                .build();
        expectedPayload = Collections.singletonMap(RESPONDENT_PIN, TEST_PIN);
    }

    @Test
    public void givenGenerateInvitationIsTrue_whenCcdCallbackHandler_thenReturnExpected()
        throws WorkflowException {
        //given
        when(issueEventWorkflow.run(ccdCallbackRequest, AUTH_TOKEN, true)).thenReturn(expectedPayload);

        //when
        Map<String, Object> actual = classUnderTest.handleIssueEventCallback(ccdCallbackRequest, AUTH_TOKEN, true);

        //then
        assertEquals(expectedPayload, actual);
        assertEquals(expectedPayload.get(RESPONDENT_PIN), TEST_PIN);
    }

    @Test
    public void givenGenerateInvitationIsFalse_whenCcdCallbackHandler_thenReturnExpected()
        throws WorkflowException {
        //given
        when(issueEventWorkflow.run(ccdCallbackRequest, AUTH_TOKEN, false)).thenReturn(expectedPayload);

        //when
        Map<String, Object> actual = classUnderTest.handleIssueEventCallback(ccdCallbackRequest, AUTH_TOKEN, false);

        //then
        assertEquals(expectedPayload, actual);
        assertEquals(expectedPayload.get(RESPONDENT_PIN), TEST_PIN);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenDraftInWorkflowResponse_whenGetDraft_thenReturnPayloadFromWorkflow() throws WorkflowException {
        Map<String, Object> testExpectedPayload = mock(Map.class);

        when(retrieveDraftWorkflow.run(AUTH_TOKEN)).thenReturn(testExpectedPayload);
        assertEquals(testExpectedPayload, classUnderTest.getDraft(AUTH_TOKEN));
    }

    @Test
    public void whenRetrieveAosCase_thenProceedAsExpected() throws WorkflowException {
        final CaseDataResponse caseDataResponse = CaseDataResponse.builder().build();

        when(retrieveAosCaseWorkflow.run(AUTH_TOKEN)).thenReturn(caseDataResponse);

        assertEquals(caseDataResponse, classUnderTest.retrieveAosCase(AUTH_TOKEN));

        verify(retrieveAosCaseWorkflow).run(AUTH_TOKEN);
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

        when(saveDraftWorkflow.run(payload, AUTH_TOKEN, Boolean.TRUE.toString())).thenReturn(testExpectedPayload);
        assertEquals(testExpectedPayload, classUnderTest.saveDraft(payload, AUTH_TOKEN, Boolean.TRUE.toString()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenErrorOnDraftWorkflow_whenSaveDraft_thenReturnErrors() throws WorkflowException {
        Map<String, Object> expectedErrors = mock(Map.class);
        Map<String, Object> payload = mock(Map.class);
        Map<String, Object> workflowResponsePayload = mock(Map.class);

        when(saveDraftWorkflow.run(payload, AUTH_TOKEN, Boolean.TRUE.toString())).thenReturn(workflowResponsePayload);
        when(saveDraftWorkflow.errors()).thenReturn(expectedErrors);

        assertEquals(expectedErrors, classUnderTest.saveDraft(payload, AUTH_TOKEN, Boolean.TRUE.toString()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenUserWithADraft_whenDeleteDraft_thenReturnPayloadFromWorkflow() throws WorkflowException {
        Map<String, Object> testExpectedPayload = mock(Map.class);
        when(deleteDraftWorkflow.run(AUTH_TOKEN)).thenReturn(testExpectedPayload);
        assertEquals(testExpectedPayload, classUnderTest.deleteDraft(AUTH_TOKEN));
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
        Map<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("returnedKey", "returnedValue");
        expectedPayload.put(ALLOCATED_COURT_KEY, "randomlyAllocatedKey");
        when(submitToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        when(submitToCCDWorkflow.errors()).thenReturn(Collections.emptyMap());

        // when
        Map<String, Object> actual = classUnderTest.submit(requestPayload, AUTH_TOKEN);

        // then
        assertThat(actual.get("returnedKey"), is("returnedValue"));
        assertThat(actual.get("returnedKey"), is("returnedValue"));

        verify(submitToCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(submitToCCDWorkflow).errors();
    }

    @Test
    public void givenCaseDataInvalid_whenSubmit_thenReturnListOfErrors() throws Exception {
        // given
        when(submitToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        Map<String, Object> errors = singletonMap("new_Error", "An Error");
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
    public void givenValidPaymentData_whenPaymentUpdate_thenReturnPayload() throws Exception {
        PaymentUpdate paymentUpdate = new PaymentUpdate();
        paymentUpdate.setCcdCaseNumber("1232132");
        paymentUpdate.setStatus("success");
        paymentUpdate.setAmount(new BigDecimal(550.00));
        Fee fee = new Fee();
        fee.setCode("X243");
        paymentUpdate.setFees(Arrays.asList(fee, fee));
        paymentUpdate.setChannel("card");

        CaseDetails caseDetails = CaseDetails.builder().state(AWAITING_PAYMENT).build();

        // given
        when(getCaseWithIdWorkflow.run(any())).thenReturn(caseDetails);

        when(updateToCCDWorkflow.run(any(), any(), any()))
            .thenReturn(requestPayload);

        when(authUtil.getCaseworkerToken()).thenReturn("testtoken");

        // when
        Map<String, Object> actual = classUnderTest.update(paymentUpdate);

        // then
        assertEquals(requestPayload, actual);

        Payment payment = Payment.builder()
            .paymentFeeId("X243")
            .paymentChannel("card")
            .paymentStatus("success")
            .paymentAmount("55000")
            .build();

        final Map<String, Object> updateEvent = new HashMap<>();
        updateEvent.put("eventData", singletonMap("payment", payment));
        updateEvent.put("eventId", "paymentMade");

        verify(updateToCCDWorkflow).run(updateEvent, "testtoken", "1232132");
    }

    @Test
    public void givenValidPaymentDataWithoutChannel_whenPaymentUpdate_thenReturnPayloadWithDefaultChannel() throws Exception {
        PaymentUpdate paymentUpdate = new PaymentUpdate();
        paymentUpdate.setCcdCaseNumber("1232132");
        paymentUpdate.setStatus("success");
        paymentUpdate.setAmount(new BigDecimal(550.00));
        Fee fee = new Fee();
        fee.setCode("X243");
        paymentUpdate.setFees(Arrays.asList(fee, fee));

        CaseDetails caseDetails = CaseDetails.builder().state(AWAITING_PAYMENT).build();

        // given
        when(getCaseWithIdWorkflow.run(any())).thenReturn(caseDetails);

        when(updateToCCDWorkflow.run(any(), any(), any()))
                .thenReturn(requestPayload);

        when(authUtil.getCaseworkerToken()).thenReturn("testtoken");

        // when
        Map<String, Object> actual = classUnderTest.update(paymentUpdate);

        // then
        assertEquals(requestPayload, actual);

        Payment payment = Payment.builder()
                .paymentFeeId("X243")
                .paymentChannel("online")
                .paymentStatus("success")
                .paymentAmount("55000")
                .build();

        final Map<String, Object> updateEvent = new HashMap<>();
        updateEvent.put("eventData", singletonMap("payment", payment));
        updateEvent.put("eventId", "paymentMade");

        verify(updateToCCDWorkflow).run(updateEvent, "testtoken", "1232132");
    }

    @Test
    public void givenValidPaymentDataButCaseInWrongState_whenPaymentUpdate_thenReturnPayload() throws Exception {
        PaymentUpdate paymentUpdate = new PaymentUpdate();
        paymentUpdate.setCcdCaseNumber("1232132");
        paymentUpdate.setStatus("success");
        paymentUpdate.setAmount(new BigDecimal(550.00));
        Fee fee = new Fee();
        fee.setCode("X243");
        paymentUpdate.setFees(Arrays.asList(fee, fee));
        paymentUpdate.setChannel("online");

        CaseDetails caseDetails = CaseDetails.builder().state("notAwaitingPayment").build();

        // given
        when(getCaseWithIdWorkflow.run(any())).thenReturn(caseDetails);

        // when
        Map<String, Object> actual = classUnderTest.update(paymentUpdate);

        // then
        assertEquals(Collections.EMPTY_MAP, actual);

        verifyZeroInteractions(updateToCCDWorkflow);
    }

    @Test
    public void givenFailedPaymentData_whenPaymentUpdate_thenReturnPayload() throws Exception {
        PaymentUpdate paymentUpdate = new PaymentUpdate();
        paymentUpdate.setStatus("failed");

        // when
        Map<String, Object> actual = classUnderTest.update(paymentUpdate);

        // then
        assertEquals(Collections.EMPTY_MAP, actual);

        verifyZeroInteractions(updateToCCDWorkflow);
    }


    @Test(expected = WorkflowException.class)
    public void givenPaymentDataWithNoAmount_whenPaymentUpdate_thenThrowWorkflowException() throws Exception {
        PaymentUpdate paymentUpdate = new PaymentUpdate();
        paymentUpdate.setCcdCaseNumber("1232132");
        paymentUpdate.setStatus("success");
        Fee fee = new Fee();
        fee.setCode("X243");
        paymentUpdate.setFees(Arrays.asList(fee, fee));
        paymentUpdate.setChannel("online");

        CaseDetails caseDetails = CaseDetails.builder().state(AWAITING_PAYMENT).build();
        when(getCaseWithIdWorkflow.run(any())).thenReturn(caseDetails);

        classUnderTest.update(paymentUpdate);
    }

    @Test(expected = WorkflowException.class)
    public void givenPaymentDataWithNoFee_whenPaymentUpdate_thenThrowWorkflowException() throws Exception {
        PaymentUpdate paymentUpdate = new PaymentUpdate();
        paymentUpdate.setCcdCaseNumber("1232132");
        paymentUpdate.setStatus("success");
        paymentUpdate.setAmount(new BigDecimal(550.00));
        paymentUpdate.setChannel("online");
        paymentUpdate.setDateCreated("2001-01-01T00:00:00.000+0000");

        CaseDetails caseDetails = CaseDetails.builder().state(AWAITING_PAYMENT).build();
        when(getCaseWithIdWorkflow.run(any())).thenReturn(caseDetails);

        classUnderTest.update(paymentUpdate);
    }

    @Test
    public void whenLinkRespondent_thenProceedAsExpected() throws WorkflowException {
        final UserDetails userDetails = UserDetails.builder().build();

        when(linkRespondentWorkflow.run(AUTH_TOKEN, TEST_CASE_ID, TEST_PIN))
            .thenReturn(userDetails);

        assertEquals(userDetails, classUnderTest.linkRespondent(AUTH_TOKEN, TEST_CASE_ID, TEST_PIN));

        verify(linkRespondentWorkflow).run(AUTH_TOKEN, TEST_CASE_ID, TEST_PIN);
    }

    @Test
    public void givenCaseData_whenSendPetitionerSubmissionNotification_thenReturnPayload() throws Exception {
        // given
        when(sendPetitionerSubmissionNotificationWorkflow.run(ccdCallbackRequest))
                .thenReturn(requestPayload);

        // when
        Map<String, Object> actual = classUnderTest.sendPetitionerSubmissionNotificationEmail(ccdCallbackRequest);

        // then
        assertEquals(requestPayload, actual);

        verify(sendPetitionerSubmissionNotificationWorkflow).run(ccdCallbackRequest);
    }

    @Test
    public void givenCaseData_whenSendPetitionerGenericEmailNotification_thenReturnPayload() throws Exception {
        // given
        when(sendPetitionerEmailNotificationWorkflow.run(ccdCallbackRequest))
                .thenReturn(requestPayload);
        // when
        Map<String, Object> actual = classUnderTest.sendPetitionerGenericUpdateNotificationEmail(ccdCallbackRequest);
        // then
        assertEquals(requestPayload, actual);
        verify(sendPetitionerEmailNotificationWorkflow).run(ccdCallbackRequest);
    }

    @Test
    public void givenCaseData_whenSendPetitionerClarificationRequestNotification_thenReturnPayload() throws Exception {
        when(sendPetitionerClarificationRequestNotificationWorkflow.run(ccdCallbackRequest)).thenReturn(requestPayload);

        final Map<String, Object> actual = classUnderTest.sendPetitionerClarificationRequestNotification(ccdCallbackRequest);

        assertThat(actual, is(requestPayload));
        verify(sendPetitionerClarificationRequestNotificationWorkflow).run(ccdCallbackRequest);
    }

    @Test
    public void givenCaseData_whenSendRespondentSubmissionNotification_thenReturnPayload() throws Exception {
        when(sendRespondentSubmissionNotificationWorkflow.run(ccdCallbackRequest)).thenReturn(requestPayload);

        Map<String, Object> returnedPayload = classUnderTest
                .sendRespondentSubmissionNotificationEmail(ccdCallbackRequest);

        assertEquals(requestPayload, returnedPayload);
        verify(sendRespondentSubmissionNotificationWorkflow).run(ccdCallbackRequest);
    }

    @Test
    public void givenCaseData_whenSetOrderSummary_thenReturnPayload() throws Exception {
        // given
        when(setOrderSummaryWorkflow.run(requestPayload))
            .thenReturn(requestPayload);

        // when
        Map<String, Object> actual = classUnderTest.setOrderSummary(ccdCallbackRequest);

        // then
        assertEquals(requestPayload, actual);

        verify(setOrderSummaryWorkflow).run(requestPayload);
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_thenReturnPayload() throws Exception {
        // given
        when(processPbaPaymentWorkflow.run(ccdCallbackRequest, AUTH_TOKEN))
                .thenReturn(requestPayload);

        // when
        Map<String, Object> actual = classUnderTest.processPbaPayment(ccdCallbackRequest, AUTH_TOKEN);

        // then
        assertEquals(requestPayload, actual);

        verify(processPbaPaymentWorkflow).run(ccdCallbackRequest, AUTH_TOKEN);
    }


    @Test
    public void givenCaseDataInvalid_whenProcessPbaPayment_thenReturnListOfErrors() throws Exception {
        // given
        when(processPbaPaymentWorkflow.run(ccdCallbackRequest, AUTH_TOKEN))
                .thenReturn(requestPayload);
        Map<String, Object> errors = Collections.singletonMap("new_Error", "An Error");
        when(processPbaPaymentWorkflow.errors()).thenReturn(errors);

        // when
        Map<String, Object> actual = classUnderTest.processPbaPayment(ccdCallbackRequest, AUTH_TOKEN);

        // then
        assertEquals(errors, actual);

        verify(processPbaPaymentWorkflow).run(ccdCallbackRequest, AUTH_TOKEN);
        verify(processPbaPaymentWorkflow, times(2)).errors();
    }

    @Test
    public void givenCaseData_whenSolicitorCreate_thenReturnPayload() throws Exception {
        // given
        when(solicitorCreateWorkflow.run(requestPayload))
            .thenReturn(requestPayload);

        // when
        Map<String, Object> actual = classUnderTest.solicitorCreate(ccdCallbackRequest);

        // then
        assertEquals(requestPayload, actual);

        verify(solicitorCreateWorkflow).run(requestPayload);
    }

    @Test
    public void givenCaseData_whenSubmitAosCase_thenReturnPayload() throws Exception {
        when(submitRespondentAosCaseWorkflow.run(requestPayload, AUTH_TOKEN, TEST_CASE_ID)).thenReturn(requestPayload);

        assertEquals(requestPayload, classUnderTest.submitRespondentAosCase(requestPayload, AUTH_TOKEN, TEST_CASE_ID));

        verify(submitRespondentAosCaseWorkflow).run(requestPayload, AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void givenCaseData_whenSubmitCoRespondentAosCase_thenReturnPayload() throws Exception {
        when(submitCoRespondentAosWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(requestPayload);

        assertEquals(requestPayload, classUnderTest.submitCoRespondentAosCase(requestPayload, AUTH_TOKEN));

        verify(submitCoRespondentAosWorkflow).run(requestPayload, AUTH_TOKEN);
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
                .data(ccdCallbackRequest.getCaseDetails().getCaseData())
                .build();

        when(dnSubmittedWorkflow
                .run(ccdCallbackRequest, AUTH_TOKEN))
                .thenReturn(ccdCallbackRequest.getCaseDetails().getCaseData());

        CcdCallbackResponse ccdResponse = classUnderTest.dnSubmitted(ccdCallbackRequest, AUTH_TOKEN);

        assertEquals(expectedResponse, ccdResponse);
    }

    @Test
    public void givenNoError_whenExecuteCoRespReceivedWorkflow_thenReturnCaseData() throws WorkflowException {
        CcdCallbackResponse expectedResponse =  CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build();

        when(sendCoRespondSubmissionNotificationWorkflow
            .run(ccdCallbackRequest))
            .thenReturn(ccdCallbackRequest.getCaseDetails().getCaseData());

        CcdCallbackResponse ccdResponse = classUnderTest.sendCoRespReceivedNotificationEmail(ccdCallbackRequest);

        assertEquals(expectedResponse, ccdResponse);
    }

    @Test
    public void givenError_whenExecuteDnSubmittedWorkflow_thenReturnErrorData() throws WorkflowException {

        Map<String, Object> workflowError = singletonMap("ErrorKey", "Error value");
        when(dnSubmittedWorkflow
                .run(ccdCallbackRequest, AUTH_TOKEN))
                .thenReturn(ccdCallbackRequest.getCaseDetails().getCaseData());

        when(dnSubmittedWorkflow.errors()).thenReturn(workflowError);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .errors(Collections.singletonList("Error value"))
            .build();

        CcdCallbackResponse ccdResponse = classUnderTest.dnSubmitted(ccdCallbackRequest, AUTH_TOKEN);

        assertEquals(expectedResponse, ccdResponse);
    }

    @Test
    public void givenCaseId_whenAmendPetition_thenReturnDraft() throws Exception {
        when(amendPetitionWorkflow.run(TEST_CASE_ID, AUTH_TOKEN)).thenReturn(requestPayload);

        assertEquals(requestPayload, classUnderTest.amendPetition(TEST_CASE_ID, AUTH_TOKEN));

        verify(amendPetitionWorkflow).run(TEST_CASE_ID, AUTH_TOKEN);
    }

    @Test
    public void testServiceCallsRightWorkflowWithRightData_ForProcessingCaseLinkedBackEvent()
        throws WorkflowException, CaseOrchestrationServiceException {
        when(caseLinkedForHearingWorkflow.run(eq(ccdCallbackRequest.getCaseDetails()))).thenReturn(requestPayload);

        assertThat(classUnderTest.processCaseLinkedForHearingEvent(ccdCallbackRequest), is(equalTo(requestPayload)));
    }

    @Test
    public void shouldThrowException_ForProcessingCaseLinkedBackEvent_WhenWorkflowExceptionIsCaught()
        throws WorkflowException, CaseOrchestrationServiceException {
        when(caseLinkedForHearingWorkflow.run(eq(ccdCallbackRequest.getCaseDetails())))
            .thenThrow(new WorkflowException("This operation threw an exception."));

        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectMessage(is("This operation threw an exception."));

        classUnderTest.processCaseLinkedForHearingEvent(ccdCallbackRequest);
    }

    @Test
    public void whenProcessAwaitingPronouncementCases_thenProceedAsExpected() throws WorkflowException {
        Map<String, Object> expectedResult = ImmutableMap.of("someKey", "someValue");
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
        when(processAwaitingPronouncementCasesWorkflow.run(AUTH_TOKEN)).thenReturn(expectedResult);

        Map<String, Object>  actual = classUnderTest.generateBulkCaseForListing();

        assertEquals(expectedResult, actual);
    }

    @Test
    public void shouldCallTheRightWorkflow_ForCoRespondentAnswersGeneratorEvent() throws WorkflowException {
        when(generateCoRespondentAnswersWorkflow.run(eq(ccdCallbackRequest.getCaseDetails()), eq(AUTH_TOKEN)))
            .thenReturn(requestPayload);

        assertThat(classUnderTest.generateCoRespondentAnswers(ccdCallbackRequest, AUTH_TOKEN),
            is(equalTo(requestPayload)));
    }

    @Test(expected = WorkflowException.class)
    public void shouldThrowException_ForCoRespondentAnswersGeneratorEvent_WhenWorkflowExceptionIsCaught()
            throws WorkflowException {
        when(generateCoRespondentAnswersWorkflow.run(eq(ccdCallbackRequest.getCaseDetails()), eq(AUTH_TOKEN)))
            .thenThrow(new WorkflowException("This operation threw an exception"));

        classUnderTest.generateCoRespondentAnswers(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void shouldCallTheRightWorkflow_ForDocumentGeneration() throws WorkflowException {
        when(documentGenerationWorkflow.run(ccdCallbackRequest, AUTH_TOKEN , "a", "b", "c"))
            .thenReturn(requestPayload);

        final Map<String, Object> result = classUnderTest
            .handleDocumentGenerationCallback(ccdCallbackRequest, AUTH_TOKEN, "a", "b", "c");

        assertThat(result, is(requestPayload));
    }

    @Test
    public void shouldCallTheRightWorkflow_ForDnPronouncementDocumentsGeneration() throws WorkflowException {
        final Map<String, Object> result = classUnderTest
            .handleDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        assertThat(result, is(requestPayload));
    }

    @Test
    public void shouldGenerateNoDocuments_whenBulkCaseLinkIdIsNull() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "No");

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
                CaseDetails.builder().caseData(caseData).build())
                .build();

        classUnderTest
                .handleDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verifyZeroInteractions(documentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateDecreeNisiDocumentOnly_WhenCostsClaimGrantedIsNo() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "No");

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseData(caseData).build())
            .build();

        classUnderTest
            .handleDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(documentGenerationWorkflow, times(1)).run(ccdCallbackRequest, AUTH_TOKEN,
                DECREE_NISI_TEMPLATE_ID, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI_FILENAME);
        verifyNoMoreInteractions(documentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateBothDocuments_WhenCostsClaimGrantedIsYes() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes");

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
                CaseDetails.builder().caseData(caseData).build())
                .build();

        classUnderTest
                .handleDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(documentGenerationWorkflow, times(1)).run(ccdCallbackRequest, AUTH_TOKEN,
                DECREE_NISI_TEMPLATE_ID, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI_FILENAME);
        verify(documentGenerationWorkflow, times(1)).run(ccdCallbackRequest, AUTH_TOKEN,
                COSTS_ORDER_TEMPLATE_ID, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_DOCUMENT_TYPE);
        verifyNoMoreInteractions(documentGenerationWorkflow);
    }

    @Test(expected = WorkflowException.class)
    public void shouldThrowException_ForDocumentGeneration_WhenWorkflowExceptionIsCaught()
            throws WorkflowException {

        when(documentGenerationWorkflow.run(ccdCallbackRequest, AUTH_TOKEN , "a", "b", "c"))
                .thenThrow(new WorkflowException("This operation threw an exception"));

        classUnderTest.handleDocumentGenerationCallback(ccdCallbackRequest, AUTH_TOKEN, "a", "b", "c");
    }

    @Test(expected = WorkflowException.class)
    public void shouldThrowException_ForDnPronouncedDocumentsGeneration_WhenWorkflowExceptionIsCaught()
            throws WorkflowException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes");

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseData(caseData).build())
            .build();

        when(documentGenerationWorkflow.run(ccdCallbackRequest, AUTH_TOKEN,
                COSTS_ORDER_TEMPLATE_ID, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_DOCUMENT_TYPE))
            .thenThrow(new WorkflowException("This operation threw an exception"));

        classUnderTest.handleDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void testServiceCallsRightWorkflowWithRightData_ForProcessingAosSolicitorNominated()
            throws WorkflowException, CaseOrchestrationServiceException {
        when(respondentSolicitorNominatedWorkflow.run(eq(ccdCallbackRequest.getCaseDetails()))).thenReturn(requestPayload);

        assertThat(classUnderTest.processAosSolicitorNominated(ccdCallbackRequest), is(equalTo(requestPayload)));
    }

    @Test
    public void shouldThrowException_ForProcessingAosSolicitorNominated_WhenWorkflowExceptionIsCaught()
            throws WorkflowException, CaseOrchestrationServiceException {
        when(respondentSolicitorNominatedWorkflow.run(eq(ccdCallbackRequest.getCaseDetails())))
                .thenThrow(new WorkflowException("This operation threw an exception."));

        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectMessage(is("This operation threw an exception."));

        classUnderTest.processAosSolicitorNominated(ccdCallbackRequest);
    }

    @Test
    public void shouldCallTheRightWorkflow_ForProcessSeparationFields() throws WorkflowException {
        when(separationFieldsWorkflow.run(eq(ccdCallbackRequest.getCaseDetails().getCaseData())))
            .thenReturn(requestPayload);

        assertThat(classUnderTest.processSeparationFields(ccdCallbackRequest),
            is(equalTo(requestPayload)));
    }

    @Test(expected = WorkflowException.class)
    public void shouldThrowException_ForProcessSeparationFields_WhenWorkflowExceptionIsCaught()
        throws WorkflowException {
        when(separationFieldsWorkflow.run(eq(ccdCallbackRequest.getCaseDetails().getCaseData())))
            .thenThrow(new WorkflowException("This operation threw an exception"));

        classUnderTest.processSeparationFields(ccdCallbackRequest);
    }

    @Test
    public void shouldCallTheRightWorkflow_ForProcessBulkCaseScheduleForHearing() throws WorkflowException {
        when(bulkCaseUpdateHearingDetailsEventWorkflow.run(eq(ccdCallbackRequest), eq(AUTH_TOKEN)))
                .thenReturn(requestPayload);

        assertThat(classUnderTest.processBulkCaseScheduleForHearing(ccdCallbackRequest, AUTH_TOKEN),
                is(equalTo(requestPayload)));
    }

    @Test(expected = WorkflowException.class)
    public void shouldThrowException_ForProcessBulkCaseScheduleForHearing_WhenWorkflowExceptionIsCaught()
            throws WorkflowException {
        when(bulkCaseUpdateHearingDetailsEventWorkflow.run(eq(ccdCallbackRequest), eq(AUTH_TOKEN)))
                .thenThrow(new WorkflowException("This operation threw an exception"));

        classUnderTest.processBulkCaseScheduleForHearing(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void shouldCallTheRightWorkflow_ForvalidateBulkCaseListingData() throws WorkflowException {
        when(validateBulkCaseListingWorkflow.run(eq(requestPayload)))
                .thenReturn(requestPayload);

        assertThat(classUnderTest.validateBulkCaseListingData(requestPayload),
                is(equalTo(requestPayload)));
    }

    @Test(expected = WorkflowException.class)
    public void shouldThrowException_ForvalidateBulkCaseListingData_WhenWorkflowExceptionIsCaught()
            throws WorkflowException {
        when(validateBulkCaseListingWorkflow.run(eq(requestPayload)))
                .thenThrow(new WorkflowException("This operation threw an exception"));

        classUnderTest.validateBulkCaseListingData(requestPayload);
    }

    @Test
    public void shouldCallWorkflow_ForDecreeNisiIsAboutToBeGranted() throws WorkflowException, CaseOrchestrationServiceException {
        when(decreeNisiAboutToBeGrantedWorkflow.run(ccdCallbackRequest.getCaseDetails())).thenReturn(singletonMap("returnedKey", "returnedValue"));

        Map<String, Object> returnedPayload = classUnderTest.processCaseBeforeDecreeNisiIsGranted(ccdCallbackRequest);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));
    }

    @Test
    public void shouldThrowServiceException_ForDecreeNisiIsAboutToBeGranted_WhenWorkflowExceptionIsCaught()
        throws WorkflowException, CaseOrchestrationServiceException {
        when(decreeNisiAboutToBeGrantedWorkflow.run(ccdCallbackRequest.getCaseDetails()))
            .thenThrow(new WorkflowException("This operation threw an exception."));

        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectMessage(is("This operation threw an exception."));
        expectedException.expectCause(is(instanceOf(WorkflowException.class)));

        classUnderTest.processCaseBeforeDecreeNisiIsGranted(ccdCallbackRequest);
    }

    @Test
    public void shouldGeneratePdfFile_ForDecreeNisiAndCostOrder_When_Costs_claim_granted_is_YES_Value()
            throws WorkflowException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes");

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
                CaseDetails.builder().caseData(caseData).build())
                .build();

        when(documentGenerationWorkflow.run(ccdCallbackRequest, AUTH_TOKEN,
                DECREE_NISI_TEMPLATE_ID, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI_FILENAME))
                .thenReturn(caseData);

        when(documentGenerationWorkflow.run(ccdCallbackRequest, AUTH_TOKEN,
                COSTS_ORDER_TEMPLATE_ID, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_DOCUMENT_TYPE))
                .thenReturn(requestPayload);

        final Map<String, Object> result = classUnderTest
                .handleDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        expectedResult.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes");
        expectedResult.putAll(requestPayload);

        assertThat(result, is(expectedResult));
    }

    @Test
    public void shouldCallWorkflow_ForBulkCaseUpdatePronouncementDate() throws WorkflowException {
        when(bulkCaseUpdateDnPronounceDatesWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN))
                .thenReturn(singletonMap("returnedKey", "returnedValue"));

        Map<String, Object> returnedPayload = classUnderTest.updateBulkCaseDnPronounce(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));
    }

    @Test
    public void shouldCallWorkflow_ForAddDnOutcomeFlag() throws WorkflowException {
        when(setDNOutcomeFlagWorkflow.run(ccdCallbackRequest.getCaseDetails()))
            .thenReturn(singletonMap("returnedKey", "returnedValue"));

        Map<String, Object> returnedPayload = classUnderTest.addDNOutcomeFlag(ccdCallbackRequest);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));
    }

    @After
    public void tearDown() {
        ccdCallbackRequest = null;
        requestPayload = null;
        expectedPayload = null;
    }

}
