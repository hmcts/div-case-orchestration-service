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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Fee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Payment;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentUpdate;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AmendPetitionForRefusalWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AmendPetitionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AuthenticateRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.BulkCaseCancelPronouncementEventWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.BulkCaseUpdateDnPronounceDatesWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.BulkCaseUpdateHearingDetailsEventWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CaseLinkedForHearingWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCallbackBulkPrintWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CleanStatusCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CreateNewAmendedCaseAndSubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DNSubmittedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DecreeNisiAboutToBeGrantedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DeleteDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GenerateCoRespondentAnswersWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GetCaseWithIdWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GetCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.IssueEventWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.MakeCaseEligibleForDecreeAbsoluteWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.PetitionerSolicitorRoleWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ProcessAwaitingPronouncementCasesWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveDNDocumentsWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveDnOutcomeCaseFlagWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveLegalAdvisorMakeDecisionFieldsWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveLinkFromListedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveLinkWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RespondentSolicitorLinkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RespondentSolicitorNominatedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SaveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendClarificationSubmittedNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendCoRespondSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerClarificationRequestNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerEmailNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SeparationFieldsWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SetOrderSummaryWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SetupConfirmServicePaymentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorCreateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorSubmissionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorUpdateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitCoRespondentAosWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDaCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitDnCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitRespondentAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ValidateBulkCaseListingWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.WelshContinueInterceptWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.WelshContinueWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.WelshSetPreviousStateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aos.AosSubmissionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.decreeabsolute.ApplicantDecreeAbsoluteEligibilityWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.decreeabsolute.DecreeAbsoluteAboutToBeGrantedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.DnSubmittedEmailNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.NotifyApplicantCanFinaliseDivorceWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.NotifyForRefusalOrderWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.SendDaGrantedNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.notification.SendPetitionerAmendEmailNotificationWorkflow;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.FILE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DECREE_ABSOLUTE_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_ENDCLAIM_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
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
    private AosSubmissionWorkflow aosSubmissionWorkflow;

    @Mock
    private SendCoRespondSubmissionNotificationWorkflow sendCoRespondSubmissionNotificationWorkflow;

    @Mock
    private SetOrderSummaryWorkflow setOrderSummaryWorkflow;

    @Mock
    private SetupConfirmServicePaymentWorkflow setupConfirmServicePaymentWorkflow;

    @Mock
    private SolicitorSubmissionWorkflow solicitorSubmissionWorkflow;

    @Mock
    private SolicitorCreateWorkflow solicitorCreateWorkflow;

    @Mock
    private SolicitorUpdateWorkflow solicitorUpdateWorkflow;

    @Mock
    private SubmitRespondentAosCaseWorkflow submitRespondentAosCaseWorkflow;

    @Mock
    private SubmitCoRespondentAosWorkflow submitCoRespondentAosWorkflow;

    @Mock
    private DNSubmittedWorkflow dnSubmittedWorkflow;

    @Mock
    private SubmitDnCaseWorkflow submitDnCaseWorkflow;

    @Mock
    private SubmitDaCaseWorkflow submitDaCaseWorkflow;

    @Mock
    private GetCaseWorkflow getCaseWorkflow;

    @Mock
    private RetrieveAosCaseWorkflow retrieveAosCaseWorkflow;

    @Mock
    private AmendPetitionWorkflow amendPetitionWorkflow;

    @Mock
    private AmendPetitionForRefusalWorkflow amendPetitionForRefusalWorkflow;

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
    private RespondentSolicitorLinkCaseWorkflow respondentSolicitorLinkCaseWorkflow;

    @Mock
    private DecreeNisiAboutToBeGrantedWorkflow decreeNisiAboutToBeGrantedWorkflow;

    @Mock
    private BulkCaseUpdateDnPronounceDatesWorkflow bulkCaseUpdateDnPronounceDatesWorkflow;

    @Mock
    private CleanStatusCallbackWorkflow cleanStatusCallbackWorkflow;

    @Mock
    private MakeCaseEligibleForDecreeAbsoluteWorkflow makeCaseEligibleForDecreeAbsoluteWorkFlow;

    @Mock
    private ApplicantDecreeAbsoluteEligibilityWorkflow applicantDecreeAbsoluteEligibilityWorkflow;

    @Mock
    private PetitionerSolicitorRoleWorkflow petitionerSolicitorRoleWorkflow;

    @Mock
    private DecreeAbsoluteAboutToBeGrantedWorkflow decreeAbsoluteAboutToBeGrantedWorkflow;

    @Mock
    private RemoveLinkWorkflow removeLinkWorkflow;

    @Mock
    private RemoveLinkFromListedWorkflow removeLinkFromListedWorkflow;

    @Mock
    private RemoveDnOutcomeCaseFlagWorkflow removeDnOutcomeCaseFlagWorkflow;

    @Mock
    private RemoveLegalAdvisorMakeDecisionFieldsWorkflow removeLegalAdvisorMakeDecisionFieldsWorkflow;

    @Mock
    private CcdCallbackBulkPrintWorkflow ccdCallbackBulkPrintWorkflow;

    @Mock
    private NotifyForRefusalOrderWorkflow notifyForRefusalOrderWorkflow;

    @Mock
    private RemoveDNDocumentsWorkflow removeDNDocumentsWorkflow;

    @Mock
    private BulkCaseCancelPronouncementEventWorkflow bulkCaseCancelPronouncementEventWorkflow;

    @Mock
    private NotifyApplicantCanFinaliseDivorceWorkflow notifyApplicantCanFinaliseDivorceWorkflow;

    @Mock
    private SendDaGrantedNotificationWorkflow sendDaGrantedNotificationWorkflow;

    @Mock
    private DnSubmittedEmailNotificationWorkflow dnSubmittedEmailNotificationWorkflow;

    @Mock
    private SendClarificationSubmittedNotificationWorkflow sendClarificationSubmittedNotificationWorkflow;

    @Mock
    private CreateNewAmendedCaseAndSubmitToCCDWorkflow createNewAmendedCaseAndSubmitToCCDWorkflow;

    @Mock
    private SendPetitionerAmendEmailNotificationWorkflow sendPetitionerAmendEmailNotificationWorkflow;

    @Mock
    private DocumentTemplateService documentTemplateService;

    @Mock
    private WelshContinueWorkflow welshContinueWorkflow;

    @Mock
    private WelshContinueInterceptWorkflow welshContinueInterceptWorkflow;

    @Mock
    private WelshSetPreviousStateWorkflow welshSetPreviousStateWorkflow;

    @InjectMocks
    private CaseOrchestrationServiceImpl classUnderTest;

    @Mock
    private AuthUtil authUtil;

    private CcdCallbackRequest ccdCallbackRequest;

    private Map<String, Object> requestPayload;

    private Map<String, Object> expectedPayload;

    private static final String COSTS_ORDER_TEMPLATE_ID = "FL-DIV-DEC-ENG-00060.docx";

    private static final String DECREE_NISI_TEMPLATE_ID = "FL-DIV-GNO-ENG-00021.docx";

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
    public void givenGenerateInvitationIsTrue_whenCcdCallbackHandler_thenReturnExpected() throws WorkflowException {
        when(issueEventWorkflow.run(ccdCallbackRequest, AUTH_TOKEN, true)).thenReturn(expectedPayload);

        Map<String, Object> actual = classUnderTest.handleIssueEventCallback(ccdCallbackRequest, AUTH_TOKEN, true);

        assertEquals(expectedPayload, actual);
        assertEquals(TEST_PIN, actual.get(RESPONDENT_PIN));
    }

    @Test
    public void givenGenerateInvitationIsFalse_whenCcdCallbackHandler_thenReturnExpected() throws WorkflowException {
        when(issueEventWorkflow.run(ccdCallbackRequest, AUTH_TOKEN, false)).thenReturn(expectedPayload);

        Map<String, Object> actual = classUnderTest.handleIssueEventCallback(ccdCallbackRequest, AUTH_TOKEN, false);

        assertEquals(expectedPayload, actual);
        assertEquals(TEST_PIN, actual.get(RESPONDENT_PIN));
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

        when(authenticateRespondentWorkflow.run(AUTH_TOKEN)).thenReturn(expected);

        Boolean actual = classUnderTest.authenticateRespondent(AUTH_TOKEN);

        assertEquals(expected, actual);

        verify(authenticateRespondentWorkflow).run(AUTH_TOKEN);
    }

    @Test
    public void givenCaseDataValid_whenSubmit_thenReturnPayload() throws Exception {
        Map<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("returnedKey", "returnedValue");
        expectedPayload.put(ALLOCATED_COURT_KEY, "randomlyAllocatedKey");
        when(submitToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        when(submitToCCDWorkflow.errors()).thenReturn(Collections.emptyMap());

        Map<String, Object> actual = classUnderTest.submit(requestPayload, AUTH_TOKEN);

        assertThat(actual.get("returnedKey"), is("returnedValue"));
        assertThat(actual.get("returnedKey"), is("returnedValue"));

        verify(submitToCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(submitToCCDWorkflow).errors();
    }

    @Test
    public void givenCaseDataInvalid_whenSubmit_thenReturnListOfErrors() throws Exception {
        when(submitToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        Map<String, Object> errors = singletonMap("new_Error", "An Error");
        when(submitToCCDWorkflow.errors()).thenReturn(errors);

        Map<String, Object> actual = classUnderTest.submit(requestPayload, AUTH_TOKEN);

        assertEquals(errors, actual);

        verify(submitToCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(submitToCCDWorkflow, times(2)).errors();
    }

    @Test
    public void givenCaseDataValid_whenUpdate_thenReturnPayload() throws Exception {
        when(updateToCCDWorkflow.run(requestPayload, AUTH_TOKEN, TEST_CASE_ID))
            .thenReturn(requestPayload);

        Map<String, Object> actual = classUnderTest.update(requestPayload, AUTH_TOKEN, TEST_CASE_ID);

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

        when(getCaseWithIdWorkflow.run(any())).thenReturn(caseDetails);
        when(updateToCCDWorkflow.run(any(), any(), any()))
            .thenReturn(requestPayload);
        when(authUtil.getCaseworkerToken()).thenReturn("testtoken");

        Map<String, Object> actual = classUnderTest.update(paymentUpdate);

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

        when(getCaseWithIdWorkflow.run(any())).thenReturn(caseDetails);
        when(updateToCCDWorkflow.run(any(), any(), any()))
            .thenReturn(requestPayload);
        when(authUtil.getCaseworkerToken()).thenReturn("testtoken");

        Map<String, Object> actual = classUnderTest.update(paymentUpdate);

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

        when(getCaseWithIdWorkflow.run(any())).thenReturn(caseDetails);

        Map<String, Object> actual = classUnderTest.update(paymentUpdate);

        assertEquals(Collections.EMPTY_MAP, actual);

        verifyNoInteractions(updateToCCDWorkflow);
    }

    @Test
    public void givenFailedPaymentData_whenPaymentUpdate_thenReturnPayload() throws Exception {
        PaymentUpdate paymentUpdate = new PaymentUpdate();
        paymentUpdate.setStatus("failed");

        Map<String, Object> actual = classUnderTest.update(paymentUpdate);

        assertEquals(Collections.EMPTY_MAP, actual);

        verifyNoInteractions(updateToCCDWorkflow);
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
        when(sendPetitionerSubmissionNotificationWorkflow.run(ccdCallbackRequest)).thenReturn(requestPayload);

        Map<String, Object> actual = classUnderTest.sendPetitionerSubmissionNotificationEmail(ccdCallbackRequest);

        assertEquals(requestPayload, actual);
        verify(sendPetitionerSubmissionNotificationWorkflow).run(ccdCallbackRequest);
    }

    @Test
    public void shouldThrowException_whenSendPetitionerSubmissionNotification_throwsWorkflowException() throws Exception {
        when(sendPetitionerSubmissionNotificationWorkflow.run(ccdCallbackRequest)).thenThrow(WorkflowException.class);

        try {
            classUnderTest.sendPetitionerSubmissionNotificationEmail(ccdCallbackRequest);
            fail("Should have caught exception");
        } catch (CaseOrchestrationServiceException exception) {
            assertThat(exception.getCause(), instanceOf(WorkflowException.class));
            Optional<String> caseId = exception.getCaseId();
            assertThat(caseId.isPresent(), is(true));
            assertThat(caseId.get(), is(TEST_CASE_ID));
        }
    }

    @Test
    public void givenCaseData_whenSendPetitionerGenericEmailNotification_thenReturnPayload() throws Exception {
        when(sendPetitionerEmailNotificationWorkflow.run(ccdCallbackRequest))
            .thenReturn(requestPayload);

        Map<String, Object> actual = classUnderTest.sendPetitionerGenericUpdateNotificationEmail(ccdCallbackRequest);

        assertEquals(requestPayload, actual);
        verify(sendPetitionerEmailNotificationWorkflow).run(ccdCallbackRequest);
    }

    @Test
    public void givenCaseData_whenSendPetitionerAmendEmailNotificationWorkflow_thenReturnPayload() throws Exception {
        when(sendPetitionerAmendEmailNotificationWorkflow.run(ccdCallbackRequest))
            .thenReturn(requestPayload);

        Map<String, Object> actual = classUnderTest.sendAmendApplicationEmail(ccdCallbackRequest);

        assertEquals(requestPayload, actual);
        verify(sendPetitionerAmendEmailNotificationWorkflow).run(ccdCallbackRequest);
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
        when(aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(requestPayload);

        Map<String, Object> returnedPayload = classUnderTest
            .aosSubmission(ccdCallbackRequest, AUTH_TOKEN);

        assertEquals(requestPayload, returnedPayload);
        verify(aosSubmissionWorkflow).run(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void givenCaseData_whenSetOrderSummary_thenReturnPayload() throws Exception {
        when(setOrderSummaryWorkflow.run(requestPayload))
            .thenReturn(requestPayload);
        when(petitionerSolicitorRoleWorkflow.run(ccdCallbackRequest, AUTH_TOKEN))
            .thenReturn(requestPayload);

        CcdCallbackResponse actual = classUnderTest.setOrderSummaryAssignRole(ccdCallbackRequest, AUTH_TOKEN);

        assertEquals(requestPayload, actual.getData());

        verify(setOrderSummaryWorkflow).run(requestPayload);
    }

    @Test
    public void givenCaseData_whenProcessPbaPayment_thenReturnPayload() throws Exception {
        when(solicitorSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN))
            .thenReturn(requestPayload);

        Map<String, Object> actual = classUnderTest.solicitorSubmission(ccdCallbackRequest, AUTH_TOKEN);

        assertEquals(requestPayload, actual);

        verify(solicitorSubmissionWorkflow).run(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void givenCaseDataInvalid_whenProcessPbaPayment_thenReturnListOfErrors() throws Exception {
        when(solicitorSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN))
            .thenReturn(requestPayload);
        Map<String, Object> errors = Collections.singletonMap("new_Error", "An Error");
        when(solicitorSubmissionWorkflow.errors()).thenReturn(errors);

        Map<String, Object> actual = classUnderTest.solicitorSubmission(ccdCallbackRequest, AUTH_TOKEN);

        assertEquals(errors, actual);

        verify(solicitorSubmissionWorkflow).run(ccdCallbackRequest, AUTH_TOKEN);
        verify(solicitorSubmissionWorkflow, times(2)).errors();
    }

    @Test
    public void givenCaseData_whenSolicitorCreate_thenReturnPayload() throws Exception {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        when(solicitorCreateWorkflow.run(caseDetails, AUTH_TOKEN))
            .thenReturn(requestPayload);

        Map<String, Object> actual = classUnderTest.solicitorCreate(ccdCallbackRequest, AUTH_TOKEN);

        assertEquals(caseDetails.getCaseData(), actual);

        verify(solicitorCreateWorkflow).run(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void givenCaseData_whenSolicitorUpdate_thenReturnPayload() throws Exception {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        when(solicitorUpdateWorkflow.run(caseDetails, AUTH_TOKEN))
            .thenReturn(requestPayload);

        Map<String, Object> actual = classUnderTest.solicitorUpdate(ccdCallbackRequest, AUTH_TOKEN);

        assertEquals(caseDetails.getCaseData(), actual);

        verify(solicitorUpdateWorkflow).run(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void givenCaseData_whenCreateNewAmendedCaseAndSubmitToCCD_thenReturnPayload() throws Exception {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        when(createNewAmendedCaseAndSubmitToCCDWorkflow.run(caseDetails, AUTH_TOKEN))
            .thenReturn(requestPayload);

        Map<String, Object> actual = classUnderTest.solicitorAmendPetitionForRefusal(ccdCallbackRequest, AUTH_TOKEN);

        assertEquals(caseDetails.getCaseData(), actual);

        verify(createNewAmendedCaseAndSubmitToCCDWorkflow).run(caseDetails, AUTH_TOKEN);
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
    public void givenDaCaseData_whenSubmitDaCase_thenReturnPayload() throws Exception {
        when(submitDaCaseWorkflow.run(requestPayload, AUTH_TOKEN, TEST_CASE_ID)).thenReturn(requestPayload);

        assertEquals(requestPayload, classUnderTest.submitDaCase(requestPayload, AUTH_TOKEN, TEST_CASE_ID));

        verify(submitDaCaseWorkflow).run(requestPayload, AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void givenNoError_whenExecuteDnSubmittedWorkflow_thenReturnCaseData() throws WorkflowException {
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build();

        when(dnSubmittedWorkflow
            .run(ccdCallbackRequest, AUTH_TOKEN))
            .thenReturn(ccdCallbackRequest.getCaseDetails().getCaseData());

        CcdCallbackResponse ccdResponse = classUnderTest.dnSubmitted(ccdCallbackRequest, AUTH_TOKEN);

        assertEquals(expectedResponse, ccdResponse);
    }

    @Test
    public void shouldCallTheRightWorkflow_forHandleDnSubmitted() throws WorkflowException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        when(dnSubmittedEmailNotificationWorkflow.run(caseDetails.getCaseId(), caseDetails.getCaseData())).thenReturn(requestPayload);

        classUnderTest.handleDnSubmitted(ccdCallbackRequest);

        verify(dnSubmittedEmailNotificationWorkflow).run(caseDetails.getCaseId(), caseDetails.getCaseData());
    }

    @Test
    public void givenNoError_whenExecuteCoRespReceivedWorkflow_thenReturnCaseData() throws WorkflowException {
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
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
    public void givenCaseId_whenAmendPetitionForRefusal_thenReturnDraft() throws Exception {
        when(amendPetitionForRefusalWorkflow.run(TEST_CASE_ID, AUTH_TOKEN)).thenReturn(requestPayload);

        assertEquals(requestPayload, classUnderTest.amendPetitionForRefusal(TEST_CASE_ID, AUTH_TOKEN));

        verify(amendPetitionForRefusalWorkflow).run(TEST_CASE_ID, AUTH_TOKEN);
    }

    @Test
    public void testServiceCallsRightWorkflowWithRightData_ForProcessingCaseLinkedBackEvent()
        throws WorkflowException, CaseOrchestrationServiceException {
        when(caseLinkedForHearingWorkflow.run(eq(ccdCallbackRequest.getCaseDetails()), eq(AUTH_TOKEN)))
            .thenReturn(requestPayload);

        assertThat(
            classUnderTest.processCaseLinkedForHearingEvent(ccdCallbackRequest, AUTH_TOKEN),
            is(equalTo(requestPayload))
        );
    }

    @Test
    public void shouldThrowException_ForProcessingCaseLinkedBackEvent_WhenWorkflowExceptionIsCaught()
        throws WorkflowException, CaseOrchestrationServiceException {
        when(caseLinkedForHearingWorkflow.run(eq(ccdCallbackRequest.getCaseDetails()), eq(AUTH_TOKEN)))
            .thenThrow(new WorkflowException("This operation threw an exception."));

        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectMessage(is("This operation threw an exception."));

        classUnderTest.processCaseLinkedForHearingEvent(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void whenProcessAwaitingPronouncementCases_thenProceedAsExpected() throws WorkflowException {
        Map<String, Object> expectedResult = ImmutableMap.of("someKey", "someValue");
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
        when(processAwaitingPronouncementCasesWorkflow.run(AUTH_TOKEN)).thenReturn(expectedResult);

        Map<String, Object> actual = classUnderTest.generateBulkCaseForListing();

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
        when(documentGenerationWorkflow.run(ccdCallbackRequest, AUTH_TOKEN, "a", "b", "c"))
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

        verifyNoInteractions(documentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateOnlyDnDocuments_WhenPetitionerCostsClaimIsNo() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "No");

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseData(caseData).build())
            .build();

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_TEMPLATE_ID))
            .thenReturn(DECREE_NISI_TEMPLATE_ID);

        classUnderTest
            .handleDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(documentGenerationWorkflow, times(1)).run(ccdCallbackRequest, AUTH_TOKEN,
            DECREE_NISI_TEMPLATE_ID, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI_FILENAME);
        verify(documentGenerationWorkflow, never()).run(ccdCallbackRequest, AUTH_TOKEN,
            COSTS_ORDER_TEMPLATE_ID, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_DOCUMENT_TYPE);
        verifyNoMoreInteractions(documentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateOnlyDnDocuments_WhenPetitionerCostsClaimIsYesButThenPetitionerEndsClaim() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "Yes");
        caseData.put(DN_COSTS_OPTIONS_CCD_FIELD, DN_COSTS_ENDCLAIM_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes");

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseData(caseData).build())
            .build();

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_TEMPLATE_ID))
            .thenReturn(DECREE_NISI_TEMPLATE_ID);

        classUnderTest
            .handleDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(documentGenerationWorkflow, times(1)).run(ccdCallbackRequest, AUTH_TOKEN,
            DECREE_NISI_TEMPLATE_ID, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI_FILENAME);
        verify(documentGenerationWorkflow, never()).run(ccdCallbackRequest, AUTH_TOKEN,
            COSTS_ORDER_TEMPLATE_ID, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_DOCUMENT_TYPE);
        verifyNoMoreInteractions(documentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateBothDocuments_WhenCostsClaimContinues() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "Yes");
        caseData.put(DN_COSTS_OPTIONS_CCD_FIELD, "Continue");
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes");

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseData(caseData).build())
            .build();

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_TEMPLATE_ID))
            .thenReturn(DECREE_NISI_TEMPLATE_ID);

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.COSTS_ORDER_TEMPLATE_ID))
            .thenReturn(COSTS_ORDER_TEMPLATE_ID);

        classUnderTest.handleDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(documentGenerationWorkflow, times(1)).run(ccdCallbackRequest, AUTH_TOKEN,
            DECREE_NISI_TEMPLATE_ID, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI_FILENAME);
        verify(documentGenerationWorkflow, times(1)).run(ccdCallbackRequest, AUTH_TOKEN,
            COSTS_ORDER_TEMPLATE_ID, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_DOCUMENT_TYPE);
        verifyNoMoreInteractions(documentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateBothDocuments_WhenCostsClaimGrantedIsNo() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, NO_VALUE);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseData(caseData).build())
            .build();

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.COSTS_ORDER_TEMPLATE_ID))
            .thenReturn(COSTS_ORDER_TEMPLATE_ID);
        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_TEMPLATE_ID))
            .thenReturn(DECREE_NISI_TEMPLATE_ID);

        classUnderTest.handleDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(documentGenerationWorkflow, times(1)).run(ccdCallbackRequest, AUTH_TOKEN,
            DECREE_NISI_TEMPLATE_ID, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI_FILENAME);
        verify(documentGenerationWorkflow, times(1)).run(ccdCallbackRequest, AUTH_TOKEN,
            COSTS_ORDER_TEMPLATE_ID, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_DOCUMENT_TYPE);
        verifyNoMoreInteractions(documentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateBothDocuments_WhenCostsClaimGrantedIsYes() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseData(caseData).build())
            .build();

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_TEMPLATE_ID))
            .thenReturn(DECREE_NISI_TEMPLATE_ID);

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.COSTS_ORDER_TEMPLATE_ID))
            .thenReturn(COSTS_ORDER_TEMPLATE_ID);

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

        when(documentGenerationWorkflow.run(ccdCallbackRequest, AUTH_TOKEN, "a", "b", "c"))
            .thenThrow(new WorkflowException("This operation threw an exception"));

        classUnderTest.handleDocumentGenerationCallback(ccdCallbackRequest, AUTH_TOKEN, "a", "b", "c");
    }

    @Test(expected = WorkflowException.class)
    public void shouldThrowException_ForDnPronouncedDocumentsGeneration_WhenWorkflowExceptionIsCaught()
        throws WorkflowException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseData(caseData).build())
            .build();

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.COSTS_ORDER_TEMPLATE_ID))
            .thenReturn(COSTS_ORDER_TEMPLATE_ID);

        when(documentGenerationWorkflow.run(ccdCallbackRequest, AUTH_TOKEN,
            COSTS_ORDER_TEMPLATE_ID, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_DOCUMENT_TYPE))
            .thenThrow(new WorkflowException("This operation threw an exception"));

        classUnderTest.handleDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void testServiceCallsRightWorkflowWithRightData_ForProcessingAosSolicitorNominated()
        throws WorkflowException, CaseOrchestrationServiceException {
        when(respondentSolicitorNominatedWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(requestPayload);

        assertThat(classUnderTest.processAosSolicitorNominated(ccdCallbackRequest, AUTH_TOKEN), is(equalTo(requestPayload)));
    }

    @Test
    public void shouldThrowException_ForProcessingAosSolicitorNominated_WhenWorkflowExceptionIsCaught()
        throws WorkflowException, CaseOrchestrationServiceException {
        when(respondentSolicitorNominatedWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenThrow(new WorkflowException("This operation threw an exception."));

        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectMessage(is("This operation threw an exception."));

        classUnderTest.processAosSolicitorNominated(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void shouldCallTheRightWorkflow_ForProcessSeparationFields() throws WorkflowException {
        when(separationFieldsWorkflow.run(eq(ccdCallbackRequest.getCaseDetails().getCaseData())))
            .thenReturn(requestPayload);

        assertThat(classUnderTest.processSeparationFields(ccdCallbackRequest), is(equalTo(requestPayload)));
    }

    @Test
    public void shouldCallTheRightWorkflow_forHandleGrantDACallback() throws WorkflowException {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .put(PRONOUNCEMENT_JUDGE_CCD_FIELD, TEST_PRONOUNCEMENT_JUDGE)
            .put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME)
            .put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME)
            .put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME)
            .put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME)
            .put(D_8_CASE_REFERENCE, TEST_CASE_ID)
            .put(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD, TEST_DECREE_ABSOLUTE_GRANTED_DATE)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseData(caseData).build())
            .build();

        classUnderTest.handleGrantDACallback(ccdCallbackRequest, AUTH_TOKEN);

        verify(decreeAbsoluteAboutToBeGrantedWorkflow).run(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void shouldCallTheRightWorkflow_forHandleDaGranted() throws WorkflowException {
        when(sendDaGrantedNotificationWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(requestPayload);

        classUnderTest.handleDaGranted(ccdCallbackRequest, AUTH_TOKEN);

        verify(sendDaGrantedNotificationWorkflow).run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN);
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
    public void shouldCallTheRightWorkflow_ForProcessCancelBulkCasePronouncement() throws WorkflowException {
        when(bulkCaseCancelPronouncementEventWorkflow.run(eq(ccdCallbackRequest), eq(AUTH_TOKEN)))
            .thenReturn(requestPayload);

        assertThat(classUnderTest.processCancelBulkCasePronouncement(ccdCallbackRequest, AUTH_TOKEN),
            is(equalTo(requestPayload)));
    }

    @Test(expected = WorkflowException.class)
    public void shouldThrowException_ForProcessCancelBulkCasePronouncement_WhenWorkflowExceptionIsCaught()
        throws WorkflowException {
        when(bulkCaseCancelPronouncementEventWorkflow.run(eq(ccdCallbackRequest), eq(AUTH_TOKEN)))
            .thenThrow(new WorkflowException("This operation threw an exception"));

        classUnderTest.processCancelBulkCasePronouncement(ccdCallbackRequest, AUTH_TOKEN);
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
    public void testServiceCallsRightWorkflowWithRightData_ForProcessingAosSolicitorLinkCase()
        throws WorkflowException, CaseOrchestrationServiceException {
        String token = "token";
        final UserDetails userDetails = UserDetails.builder().build();
        when(respondentSolicitorLinkCaseWorkflow.run(eq(ccdCallbackRequest.getCaseDetails()), eq(token)))
            .thenReturn(userDetails);

        assertThat(classUnderTest.processAosSolicitorLinkCase(ccdCallbackRequest, token), is(equalTo(requestPayload)));
    }

    @Test
    public void shouldThrowException_ForProcessingAosSolicitorLinkCase_WhenWorkflowExceptionIsCaught()
        throws WorkflowException, CaseOrchestrationServiceException {
        String token = "token";
        when(respondentSolicitorLinkCaseWorkflow.run(eq(ccdCallbackRequest.getCaseDetails()), eq(token)))
            .thenThrow(new WorkflowException("This operation threw an exception."));

        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectMessage(is("This operation threw an exception."));

        classUnderTest.processAosSolicitorLinkCase(ccdCallbackRequest, token);
    }

    @Test
    public void shouldCallWorkflow_ForDecreeNisiIsAboutToBeGranted() throws WorkflowException, CaseOrchestrationServiceException {
        when(decreeNisiAboutToBeGrantedWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(singletonMap("returnedKey", "returnedValue"));

        Map<String, Object> returnedPayload = classUnderTest.processCaseBeforeDecreeNisiIsGranted(ccdCallbackRequest, AUTH_TOKEN);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));
    }

    @Test
    public void shouldThrowServiceException_ForDecreeNisiIsAboutToBeGranted_WhenWorkflowExceptionIsCaught()
        throws WorkflowException, CaseOrchestrationServiceException {
        when(decreeNisiAboutToBeGrantedWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenThrow(new WorkflowException("This operation threw an exception."));

        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectMessage(is("This operation threw an exception."));
        expectedException.expectCause(is(instanceOf(WorkflowException.class)));

        classUnderTest.processCaseBeforeDecreeNisiIsGranted(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void shouldGeneratePdfFile_ForDecreeNisiAndCostOrder_When_Costs_claim_granted_is_YES_Value()
        throws WorkflowException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID));
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "Yes");
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes");
        caseData.put(LANGUAGE_PREFERENCE_WELSH, "No");

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseData(caseData).build())
            .build();


        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_TEMPLATE_ID))
            .thenReturn(DECREE_NISI_TEMPLATE_ID);


        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH, DocumentType.COSTS_ORDER_TEMPLATE_ID))
            .thenReturn(COSTS_ORDER_TEMPLATE_ID);


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
        expectedResult.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE);
        expectedResult.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE);
        expectedResult.put(LANGUAGE_PREFERENCE_WELSH, NO_VALUE);
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
    public void shouldCallCleanStatusCallbackWorkflow() throws WorkflowException {
        when(cleanStatusCallbackWorkflow.run(ccdCallbackRequest, AUTH_TOKEN))
            .thenReturn(singletonMap("returnedKey", "returnedValue"));

        Map<String, Object> returnedPayload = classUnderTest.cleanStateCallback(ccdCallbackRequest, AUTH_TOKEN);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));
    }

    @Test
    public void testThatWorkflowIsCalled_ForMakeCaseEligibleForDA() throws WorkflowException, CaseOrchestrationServiceException {
        when(makeCaseEligibleForDecreeAbsoluteWorkFlow.run("testToken", "testCaseId")).thenReturn(expectedPayload);

        Map<String, Object> returnedPayload = classUnderTest.makeCaseEligibleForDA("testToken", "testCaseId");

        assertThat(returnedPayload, equalTo(expectedPayload));
    }

    @Test
    public void testThatWhenWorkflowThrowsException_ForMakeCaseEligibleForDA_ErrorMessagesAreReturned()
        throws WorkflowException, CaseOrchestrationServiceException {

        when(makeCaseEligibleForDecreeAbsoluteWorkFlow.run("testToken", "testCaseId")).thenThrow(new WorkflowException("Something failed"));
        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectMessage("Something failed");

        classUnderTest.makeCaseEligibleForDA("testToken", "testCaseId");
    }

    @Test
    public void shouldCallRightWorkflow_WhenProcessingCaseToBeMadeEligibleForDAForPetitioner()
        throws CaseOrchestrationServiceException, WorkflowException {
        when(applicantDecreeAbsoluteEligibilityWorkflow.run(any(), any())).thenReturn(expectedPayload);

        Map<String, Object> returnedPayload = classUnderTest.processApplicantDecreeAbsoluteEligibility(ccdCallbackRequest);

        assertThat(returnedPayload, equalTo(expectedPayload));
        verify(applicantDecreeAbsoluteEligibilityWorkflow).run(eq(TEST_CASE_ID), eq(requestPayload));
    }

    @Test
    public void shouldThrowNewException_IfExceptionIsThrown_WhenProcessingCaseToBeMadeEligibleForDAForPetitioner()
        throws CaseOrchestrationServiceException, WorkflowException {

        WorkflowException testFailureCause = new WorkflowException("Not good...");
        when(applicantDecreeAbsoluteEligibilityWorkflow.run(any(), any())).thenThrow(testFailureCause);
        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectCause(equalTo(testFailureCause));

        classUnderTest.processApplicantDecreeAbsoluteEligibility(ccdCallbackRequest);
    }

    @Test
    public void shouldCallTheRightWorkflow_forHandleMakeCaseEligibleForDaSubmitted() throws WorkflowException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        when(notifyApplicantCanFinaliseDivorceWorkflow.run(caseDetails.getCaseId(), caseDetails.getCaseData()))
            .thenReturn(requestPayload);

        classUnderTest.handleMakeCaseEligibleForDaSubmitted(ccdCallbackRequest);

        verify(notifyApplicantCanFinaliseDivorceWorkflow).run(caseDetails.getCaseId(), caseDetails.getCaseData());
    }

    @Test
    public void shouldCallRightWorkflow_WhenRemoveBulkLink() throws WorkflowException {
        Map<String, Object> caseData = DUMMY_CASE_DATA;
        CcdCallbackRequest request = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails
                .builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .build()).build();

        when(removeLinkWorkflow.run(request.getCaseDetails().getCaseData())).thenReturn(caseData);
        classUnderTest.removeBulkLink(request);


        Map<String, Object> response = classUnderTest.removeBulkLink(request);
        assertThat(response, is(caseData));
    }

    @Test
    public void givenBulkCaseWithoutJudge_whenEditBulkCase_thenReturnCaseWithoutDocument() throws Exception {
        when(validateBulkCaseListingWorkflow.run(ccdCallbackRequest.getCaseDetails().getCaseData())).thenReturn(expectedPayload);
        Map<String, Object> returnedPayload = classUnderTest.editBulkCaseListingData(ccdCallbackRequest, FILE_NAME,
            TEMPLATE_ID, DOCUMENT_TYPE, AUTH_TOKEN);

        assertThat(returnedPayload, equalTo(expectedPayload));
        verify(documentGenerationWorkflow, never()).run(any(), any(), any(), any(), any());
    }

    @Test
    public void givenBulkCaseWithJudge_whenEditBulkCase_thenReturnGenerateDocumentCalled() throws Exception {
        ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .caseData(ImmutableMap.of(PRONOUNCEMENT_JUDGE_CCD_FIELD, "Judge"))
                    .caseId(TEST_CASE_ID)
                    .state(TEST_STATE)
                    .build())
            .eventId(TEST_EVENT_ID)
            .token(TEST_TOKEN)
            .build();

        when(validateBulkCaseListingWorkflow.run(ccdCallbackRequest.getCaseDetails().getCaseData())).thenReturn(expectedPayload);
        when((documentGenerationWorkflow).run(ccdCallbackRequest, AUTH_TOKEN, TEMPLATE_ID, DOCUMENT_TYPE, FILE_NAME))
            .thenReturn(expectedPayload);

        Map<String, Object> returnedPayload = classUnderTest
            .editBulkCaseListingData(ccdCallbackRequest, FILE_NAME, TEMPLATE_ID, DOCUMENT_TYPE, AUTH_TOKEN);

        assertThat(returnedPayload, equalTo(expectedPayload));
        verify(documentGenerationWorkflow, times(1))
            .run(ccdCallbackRequest, AUTH_TOKEN, TEMPLATE_ID, DOCUMENT_TYPE, FILE_NAME);
    }

    @Test
    public void shouldCallRightWorkflow_WhenRemoveBulkListed() throws WorkflowException {
        Map<String, Object> caseData = DUMMY_CASE_DATA;
        CcdCallbackRequest request = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails
                .builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .build()).build();

        when(removeLinkFromListedWorkflow.run(request.getCaseDetails().getCaseData())).thenReturn(caseData);
        classUnderTest.removeBulkLink(request);


        Map<String, Object> response = classUnderTest.removeBulkListed(request);
        assertThat(response, is(caseData));
    }

    @Test
    public void shouldCallRightWorkflow_WhenRemoveDnOutcomeCase() throws WorkflowException {
        Map<String, Object> caseData = DUMMY_CASE_DATA;
        CcdCallbackRequest request = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails
                .builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .build()).build();

        when(removeDnOutcomeCaseFlagWorkflow.run(request)).thenReturn(caseData);

        Map<String, Object> response = classUnderTest.removeDnOutcomeCaseFlag(request);
        assertThat(response, is(caseData));
    }

    @Test
    public void shouldCallRightWorkflow_WhenRemoveLegalAdvisorMakeDecisionFields() throws WorkflowException {
        Map<String, Object> caseData = DUMMY_CASE_DATA;
        CcdCallbackRequest request = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails
                .builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .build()).build();

        when(removeLegalAdvisorMakeDecisionFieldsWorkflow.run(request)).thenReturn(caseData);

        Map<String, Object> response = classUnderTest.removeLegalAdvisorMakeDecisionFields(request);
        assertThat(response, is(caseData));
    }

    @Test
    public void shouldCallRightWorkflow_WhenCcdCallbackConfirmPersonalServiceCalled()
        throws WorkflowException {
        requestPayload = singletonMap(OrchestrationConstants.SEND_VIA_EMAIL_OR_POST, OrchestrationConstants.SEND_VIA_POST);
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

        when(ccdCallbackBulkPrintWorkflow.run(any(), any())).thenReturn(requestPayload);

        Map<String, Object> returnedPayload = classUnderTest.ccdCallbackConfirmPersonalService(ccdCallbackRequest, AUTH_TOKEN);

        assertThat(returnedPayload, equalTo(requestPayload));
        verify(ccdCallbackBulkPrintWorkflow).run(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test(expected = WorkflowException.class)
    public void shouldThrowException_IfExceptionIsThrown_WhenProcessingCcdCallbackConfirmPersonalService()
        throws WorkflowException {
        requestPayload = singletonMap(OrchestrationConstants.SEND_VIA_EMAIL_OR_POST, OrchestrationConstants.SEND_VIA_POST);
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

        WorkflowException testFailureCause = new WorkflowException("Unable to generate bulk print...");
        when(ccdCallbackBulkPrintWorkflow.run(any(), any())).thenThrow(testFailureCause);
        classUnderTest.ccdCallbackConfirmPersonalService(ccdCallbackRequest, AUTH_TOKEN);
    }

    @Test
    public void shouldNotCallBulkPrint_IfNotSendViaPost_WhenProcessingCcdCallbackConfirmPersonalService()
        throws WorkflowException {

        requestPayload = singletonMap(OrchestrationConstants.SEND_VIA_EMAIL_OR_POST, OrchestrationConstants.SEND_VIA_EMAIL_OR_POST);
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

        Map<String, Object> returnedPayload = classUnderTest.ccdCallbackConfirmPersonalService(ccdCallbackRequest, AUTH_TOKEN);

        assertThat(returnedPayload, equalTo(requestPayload));
        verifyNoInteractions(ccdCallbackBulkPrintWorkflow);
    }

    @Test
    public void shouldCallTheRefusalOrderClarificationNotifyWorkflow() throws WorkflowException {
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

        when(notifyForRefusalOrderWorkflow.run(eq(ccdCallbackRequest.getCaseDetails()))).thenReturn(requestPayload);

        classUnderTest.notifyForRefusalOrder(ccdCallbackRequest);

        verify(notifyForRefusalOrderWorkflow).run(eq(ccdCallbackRequest.getCaseDetails()));
    }

    @Test
    public void shouldCallRemoveDNGrantedDocumentsWorkflow() throws WorkflowException {
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

        when(removeDNDocumentsWorkflow.run(eq(requestPayload))).thenReturn(requestPayload);

        classUnderTest.removeDNGrantedDocuments(ccdCallbackRequest);

        verify(removeDNDocumentsWorkflow).run(eq(requestPayload));
    }

    @Test
    public void shouldCallTheRightWorkflow_whenClarificationSubmitted() throws WorkflowException {
        when(sendClarificationSubmittedNotificationWorkflow.run(ccdCallbackRequest)).thenReturn(requestPayload);

        assertThat(classUnderTest.sendClarificationSubmittedNotificationEmail(ccdCallbackRequest),
            is(CcdCallbackResponse.builder().data(requestPayload).build()));
    }

    @Test
    public void shouldReturnError_whenWorkflowExecutedWithErrors() throws WorkflowException {
        Map<String, Object> errorMap = ImmutableMap.of("ErrorKey", "ErrorValue");
        when(sendClarificationSubmittedNotificationWorkflow.run(ccdCallbackRequest)).thenReturn(requestPayload);
        when(sendClarificationSubmittedNotificationWorkflow.errors()).thenReturn(errorMap);
        assertThat(classUnderTest.sendClarificationSubmittedNotificationEmail(ccdCallbackRequest),
            is(CcdCallbackResponse.builder()
                .errors(Collections.singletonList("ErrorValue"))
                .build()));
    }

    @Test
    public void welshContinue() throws WorkflowException {
        Map<String, Object> caseData = Collections.EMPTY_MAP;
        ccdCallbackRequest = CcdCallbackRequest.builder().build();
        when(authUtil.getCaseworkerToken()).thenReturn("AUTH_TOKEN");
        when(welshContinueWorkflow.run(ccdCallbackRequest, authUtil.getCaseworkerToken()))
            .thenReturn(caseData);

        Map<String, Object> result = classUnderTest.welshContinue(ccdCallbackRequest);
        assertThat(result, is(caseData));
        verify(welshContinueWorkflow).run(ccdCallbackRequest, authUtil.getCaseworkerToken());
    }

    @Test
    public void welshContinueInterceptSuccess() throws WorkflowException {
        Map<String, Object> caseData = Collections.EMPTY_MAP;
        CaseDetails caseDetails = CaseDetails.builder().caseId("999").build();
        ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        when(welshContinueInterceptWorkflow.run(ccdCallbackRequest, authUtil.getCaseworkerToken()))
            .thenReturn(caseData);
        when(welshContinueInterceptWorkflow.errors()).thenReturn(Collections.EMPTY_MAP);

        CcdCallbackResponse ccdCallbackResponse = classUnderTest.welshContinueIntercept(ccdCallbackRequest, authUtil.getCaseworkerToken());

        assertThat(ccdCallbackResponse.getData(), is(caseData));
    }

    @Test
    public void welshContinueInterceptFailure() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseId("999").build();
        ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        Map<String, Object> workflowErrors = ImmutableMap.of("Key1", "value1", "key2", "value2", "key3", "value3");

        when(welshContinueInterceptWorkflow.errors()).thenReturn(workflowErrors);

        CcdCallbackResponse ccdCallbackResponse = classUnderTest.welshContinueIntercept(ccdCallbackRequest, authUtil.getCaseworkerToken());

        List<String> errors = workflowErrors.values().stream().map(String.class::cast).collect(Collectors.toList());
        String collect = workflowErrors.values().stream().map(String.class::cast).collect(Collectors.joining(":"));
        System.out.println(collect);
        assertThat(ccdCallbackResponse.getErrors(), is(errors));
    }

    @Test
    public void welshSetPreviousStateSuccess() throws WorkflowException {
        Map<String, Object> caseData = Collections.EMPTY_MAP;
        CaseDetails caseDetails = CaseDetails.builder().caseId("999").build();
        ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        when(authUtil.getCaseworkerToken()).thenReturn("AUTH_TOKEN");
        when(welshSetPreviousStateWorkflow.run(ccdCallbackRequest, authUtil.getCaseworkerToken()))
            .thenReturn(caseData);
        when(welshSetPreviousStateWorkflow.errors()).thenReturn(Collections.EMPTY_MAP);

        CcdCallbackResponse ccdCallbackResponse = classUnderTest.welshSetPreviousState(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getData(), is(caseData));
    }

    @Test
    public void welshSetPreviousStateFailure() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseId("999").build();
        ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        when(authUtil.getCaseworkerToken()).thenReturn("AUTH_TOKEN");
        Map<String, Object> workflowErrors = ImmutableMap.of("Key1", "value1", "key2", "value2", "key3", "value3");

        when(welshSetPreviousStateWorkflow.errors()).thenReturn(workflowErrors);

        CcdCallbackResponse ccdCallbackResponse = classUnderTest.welshSetPreviousState(ccdCallbackRequest);

        List<String> errors = workflowErrors.values().stream().map(String.class::cast).collect(Collectors.toList());
        assertThat(ccdCallbackResponse.getErrors(), is(errors));
    }

    @Test
    public void givenCaseData_whenSetupConfirmServicePaymentEvent_thenReturnPayload() throws Exception {
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

        when(setupConfirmServicePaymentWorkflow.run(eq(ccdCallbackRequest))).thenReturn(requestPayload);

        classUnderTest.setupConfirmServicePaymentEvent(ccdCallbackRequest);

        verify(setupConfirmServicePaymentWorkflow).run(eq(ccdCallbackRequest));
    }

    @Test
    public void shouldThrowException_whenSetupConfirmServicePaymentEventFeeWorkflow_throwsWorkflowException() throws Exception {
        when(setupConfirmServicePaymentWorkflow.run(ccdCallbackRequest)).thenThrow(WorkflowException.class);

        try {
            classUnderTest.setupConfirmServicePaymentEvent(ccdCallbackRequest);
            fail("Should have caught exception");
        } catch (CaseOrchestrationServiceException exception) {
            assertThat(exception.getCause(), instanceOf(WorkflowException.class));
            Optional<String> caseId = exception.getCaseId();
            assertThat(caseId.isPresent(), is(true));
            assertThat(caseId.get(), is(TEST_CASE_ID));
        }
    }

    @After
    public void tearDown() {
        ccdCallbackRequest = null;
        requestPayload = null;
        expectedPayload = null;
    }

}