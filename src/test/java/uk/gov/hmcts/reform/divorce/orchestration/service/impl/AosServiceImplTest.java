package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendEmailNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aos.AosNotReceivedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aos.AosOfflineTriggerRequestWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aos.AosOverdueEligibilityWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aos.AosOverdueForAlternativeServiceCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aos.AosOverdueWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline.AosPackOfflineAnswersWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline.IssueAosPackOfflineWorkflow;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_PAYLOAD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PAYLOAD_TO_RETURN;
import static uk.gov.hmcts.reform.divorce.orchestration.controller.util.CallbackControllerTestUtils.assertCaseOrchestrationServiceExceptionIsSetProperly;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.alternativeservice.AlternativeServiceType.SERVED_BY_ALTERNATIVE_METHOD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.alternativeservice.AlternativeServiceType.SERVED_BY_PROCESS_SERVER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;

@RunWith(MockitoJUnitRunner.class)
public class AosServiceImplTest {

    private static final String ISSUE_AOS_OFFLINE_RESPONDENT_FROM_AOS_AWAITING = "IssueAOSOffline_Respondent_fromAosAwaiting";

    @Mock
    private IssueAosPackOfflineWorkflow issueAosPackOfflineWorkflow;

    @Mock
    private AosPackOfflineAnswersWorkflow aosPackOfflineAnswersWorkflow;

    @Mock
    private AosOverdueEligibilityWorkflow aosOverdueEligibilityWorkflow;

    @Mock
    private AosOverdueWorkflow aosOverdueWorkflow;

    @Mock
    private AosOverdueForAlternativeServiceCaseWorkflow aosOverdueForAlternativeServiceCaseWorkflow;

    @Mock
    private AosNotReceivedWorkflow aosNotReceivedWorkflow;

    @Mock
    private AosOfflineTriggerRequestWorkflow aosOfflineTriggerRequestWorkflow;

    @Mock
    private SendEmailNotificationWorkflow sendEmailNotificationWorkflow;

    @Mock
    private UpdateCaseWorkflow updateCaseWorkflow;

    @InjectMocks
    private AosServiceImpl classUnderTest;

    private String testAuthToken;
    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        testAuthToken = "testAuthToken";
        caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(TEST_INCOMING_PAYLOAD).build();
    }

    @Test
    public void testWorkflowIsCalledWithRightParams() throws WorkflowException, CaseOrchestrationServiceException {
        when(issueAosPackOfflineWorkflow.run(any(), any(), any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        Map<String, Object> result = classUnderTest.issueAosPackOffline(testAuthToken, caseDetails, RESPONDENT);

        assertThat(result, equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(issueAosPackOfflineWorkflow).run(eq(testAuthToken), eq(caseDetails), eq(RESPONDENT));
    }

    @Test
    public void shouldCallWorkflowForCoRespondentIfReasonIsAdultery() throws WorkflowException, CaseOrchestrationServiceException {
        Map<String, Object> caseData = singletonMap(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());
        caseDetails.setCaseData(caseData);
        when(issueAosPackOfflineWorkflow.run(any(), any(), any())).thenReturn(caseData);

        Map<String, Object> result = classUnderTest.issueAosPackOffline(testAuthToken, caseDetails, CO_RESPONDENT);

        assertThat(result, equalTo(caseData));
        verify(issueAosPackOfflineWorkflow).run(eq(testAuthToken), eq(caseDetails), eq(CO_RESPONDENT));
    }

    @Test
    public void shouldNotCallWorkflowForCoRespondentIfReasonIsNotAdultery() throws CaseOrchestrationServiceException {
        caseDetails.setCaseData(singletonMap(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue()));

        CaseOrchestrationServiceException exception = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.issueAosPackOffline(testAuthToken, caseDetails, CO_RESPONDENT)
        );
        assertThat(
            exception.getMessage(),
            is(format("Co-respondent AOS pack (offline) cannot be issued for reason \"%s\"", SEPARATION_TWO_YEARS.getValue()))
        );
    }

    @Test
    public void shouldThrowMappedCaseOrchestrationServiceException() throws WorkflowException {
        when(issueAosPackOfflineWorkflow.run(any(), any(), any())).thenThrow(WorkflowException.class);

        CaseOrchestrationServiceException exception = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.issueAosPackOffline(AUTH_TOKEN, caseDetails, RESPONDENT)
        );

        assertThat(exception.getCaseId().get(), is(TEST_CASE_ID));
        assertThat(exception.getCause(), is(instanceOf(WorkflowException.class)));
    }

    @Test
    public void shouldIssueAosPackOfflineThrowCaseOrchestrationServiceException() {
        assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.issueAosPackOffline(AUTH_TOKEN, caseDetails, CO_RESPONDENT)
        );

        verifyNoInteractions(issueAosPackOfflineWorkflow);
    }

    @Test
    public void shouldCallWorkflowAndReturnPayload() throws WorkflowException, CaseOrchestrationServiceException {
        when(aosPackOfflineAnswersWorkflow.run(any(), any(), any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        Map<String, Object> returnedPayload = classUnderTest.processAosPackOfflineAnswers(AUTH_TOKEN, caseDetails, RESPONDENT);

        assertThat(returnedPayload, equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(aosPackOfflineAnswersWorkflow).run(eq(AUTH_TOKEN), eq(caseDetails), eq(RESPONDENT));
    }

    @Test
    public void shouldThrowServiceException_WhenWorkflowExceptionIsThrown() throws WorkflowException {
        when(aosPackOfflineAnswersWorkflow.run(any(), any(CaseDetails.class), notNull())).thenThrow(WorkflowException.class);

        CaseOrchestrationServiceException exception = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.processAosPackOfflineAnswers(AUTH_TOKEN, caseDetails, RESPONDENT)
        );
        assertThat(exception.getCause(), is(instanceOf(WorkflowException.class)));
    }

    @Test
    public void shouldCallAppropriateWorkflowWhenMarkingCasesToBeMovedToAosOverdue() throws WorkflowException, CaseOrchestrationServiceException {
        classUnderTest.findCasesForWhichAosIsOverdue(AUTH_TOKEN);

        verify(aosOverdueEligibilityWorkflow).run(AUTH_TOKEN);
    }

    @Test
    public void shouldThrowAppropriateException_WhenCatchingWorkflowException() throws WorkflowException {
        doThrow(WorkflowException.class).when(aosOverdueEligibilityWorkflow).run(AUTH_TOKEN);

        CaseOrchestrationServiceException exception = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.findCasesForWhichAosIsOverdue(AUTH_TOKEN)
        );
        assertThat(exception.getCause(), is(instanceOf(WorkflowException.class)));

        verify(aosOverdueEligibilityWorkflow).run(AUTH_TOKEN);
    }

    @Test
    public void shouldCallAppropriateWorkflow_WhenMakingCasesAosOverdue() throws WorkflowException, CaseOrchestrationServiceException {
        classUnderTest.makeCaseAosOverdue(AUTH_TOKEN, TEST_CASE_ID);

        verify(aosOverdueWorkflow).run(AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void shouldThrowAppropriateException_WhenCatchingWorkflowException_AosOverdue() throws WorkflowException {
        doThrow(WorkflowException.class).when(aosOverdueWorkflow).run(AUTH_TOKEN, TEST_CASE_ID);

        CaseOrchestrationServiceException exception = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.makeCaseAosOverdue(AUTH_TOKEN, TEST_CASE_ID)
        );
        assertThat(exception.getCaseId().get(), is(TEST_CASE_ID));
        assertThat(exception.getCause(), is(instanceOf(WorkflowException.class)));
    }

    @Test
    public void shouldCallWorkflow_WhenPreparingAosNotReceivedEventForSubmission() throws WorkflowException, CaseOrchestrationServiceException {
        when(aosNotReceivedWorkflow.prepareForSubmission(any(), any(), any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        Map<String, Object> returnedCaseData = classUnderTest.prepareAosNotReceivedEventForSubmission(AUTH_TOKEN, caseDetails);

        assertThat(returnedCaseData, equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(aosNotReceivedWorkflow).prepareForSubmission(AUTH_TOKEN, TEST_CASE_ID, TEST_INCOMING_PAYLOAD);
    }

    @Test
    public void shouldThrowAppropriateException_WhenCatchingWorkflowException_PreparingAosNotReceivedEventForSubmission() throws WorkflowException {
        when(aosNotReceivedWorkflow.prepareForSubmission(any(), any(), any())).thenThrow(WorkflowException.class);

        try {
            classUnderTest.prepareAosNotReceivedEventForSubmission(AUTH_TOKEN, caseDetails);
            fail("Should have thrown exception");
        } catch (CaseOrchestrationServiceException exception) {
            assertCaseOrchestrationServiceExceptionIsSetProperly(exception);
        }
    }

    @Test
    public void shouldCallAppropriateWorkflowForMovingProcessServerCaseToAwaitingDecreeNisi()
        throws WorkflowException, CaseOrchestrationServiceException {
        classUnderTest.markAosNotReceivedForProcessServerCase(AUTH_TOKEN, TEST_CASE_ID);

        verify(aosOverdueForAlternativeServiceCaseWorkflow).run(AUTH_TOKEN, TEST_CASE_ID, SERVED_BY_PROCESS_SERVER);
    }

    @Test
    public void shouldThrowCaseOrchestrationWhenMovingProcessServerCaseToAwaitingDecreeNisiFails() throws WorkflowException {
        doThrow(WorkflowException.class)
            .when(aosOverdueForAlternativeServiceCaseWorkflow).run(AUTH_TOKEN, TEST_CASE_ID, SERVED_BY_PROCESS_SERVER);

        CaseOrchestrationServiceException exception = assertThrows(CaseOrchestrationServiceException.class,
            () -> classUnderTest.markAosNotReceivedForProcessServerCase(AUTH_TOKEN, TEST_CASE_ID));

        assertThat(exception.getCaseId().get(), is(TEST_CASE_ID));
        assertThat(exception.getCause(), isA(WorkflowException.class));
    }

    @Test
    public void shouldCallAppropriateWorkflowForMovingAlternativeMethodCaseToAwaitingDecreeNisi()
        throws WorkflowException, CaseOrchestrationServiceException {
        classUnderTest.markAosNotReceivedForAlternativeMethodCase(AUTH_TOKEN, TEST_CASE_ID);

        verify(aosOverdueForAlternativeServiceCaseWorkflow).run(AUTH_TOKEN, TEST_CASE_ID, SERVED_BY_ALTERNATIVE_METHOD);
    }

    @Test
    public void shouldThrowCaseOrchestrationWhenMovingAlternativeMethodCaseToAwaitingDecreeNisiFails() throws WorkflowException {
        doThrow(WorkflowException.class)
            .when(aosOverdueForAlternativeServiceCaseWorkflow).run(AUTH_TOKEN, TEST_CASE_ID, SERVED_BY_ALTERNATIVE_METHOD);

        CaseOrchestrationServiceException exception = assertThrows(CaseOrchestrationServiceException.class,
            () -> classUnderTest.markAosNotReceivedForAlternativeMethodCase(AUTH_TOKEN, TEST_CASE_ID));

        assertThat(exception.getCaseId().get(), is(TEST_CASE_ID));
        assertThat(exception.getCause(), isA(WorkflowException.class));
    }

    @Test
    public void shouldCallAosOfflineTriggerRequestWorkflow_WhenAosHasBeenIssued_AndRespondentIsRepresentedByNonRegisteredSolicitor()
        throws WorkflowException, CaseOrchestrationServiceException {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(Map.of(RESP_SOL_REPRESENTED, YES_VALUE))
            .build();

        classUnderTest.runActionsAfterAosHasBeenIssued(TEST_EVENT_ID, caseDetails);

        verify(sendEmailNotificationWorkflow).run(TEST_EVENT_ID, caseDetails);
        verify(aosOfflineTriggerRequestWorkflow).requestAosOfflineToBeTriggered(TEST_CASE_ID);
    }

    @Test
    public void shouldNotCallAosOfflineTriggerRequestWorkflow_WhenAosHasBeenIssued_AndRespondentIsNotRepresented()
        throws WorkflowException, CaseOrchestrationServiceException {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(Map.of())
            .build();

        classUnderTest.runActionsAfterAosHasBeenIssued(TEST_EVENT_ID, caseDetails);

        verify(sendEmailNotificationWorkflow).run(TEST_EVENT_ID, caseDetails);
        verify(aosOfflineTriggerRequestWorkflow, never()).requestAosOfflineToBeTriggered(TEST_CASE_ID);
    }

    @Test
    public void shouldNotCallAosOfflineTriggerRequestWorkflow_WhenAosHasBeenIssued_AndRespondentIsRepresentedByRegisteredSolicitor()
        throws WorkflowException, CaseOrchestrationServiceException {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(Map.of(
                RESP_SOL_REPRESENTED, YES_VALUE,
                RESPONDENT_SOLICITOR_ORGANISATION_POLICY, Map.of("Organisation", Map.of("OrganisationID", "testRegisteredSolicitorOrganisationId"))
            ))
            .build();

        classUnderTest.runActionsAfterAosHasBeenIssued(TEST_EVENT_ID, caseDetails);

        verify(sendEmailNotificationWorkflow).run(TEST_EVENT_ID, caseDetails);
        verify(aosOfflineTriggerRequestWorkflow, never()).requestAosOfflineToBeTriggered(TEST_CASE_ID);
    }

    @Test
    public void shouldEncapsulateWorkflowExceptionInServiceException_WhenAosHasBeenIssued_WhenSendingNotifications() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(Map.of(RESP_SOL_REPRESENTED, YES_VALUE))
            .build();

        when(sendEmailNotificationWorkflow.run(TEST_EVENT_ID, caseDetails)).thenThrow(WorkflowException.class);

        CaseOrchestrationServiceException serviceException = assertThrows(CaseOrchestrationServiceException.class,
            () -> classUnderTest.runActionsAfterAosHasBeenIssued(TEST_EVENT_ID, caseDetails));
        assertThat(serviceException.getCaseId().get(), equalTo(TEST_CASE_ID));
        assertThat(serviceException.getCause(), isA(WorkflowException.class));

        verify(sendEmailNotificationWorkflow).run(TEST_EVENT_ID, caseDetails);
        verify(aosOfflineTriggerRequestWorkflow, never()).requestAosOfflineToBeTriggered(TEST_CASE_ID);
    }

    @Test
    public void shouldEncapsulateWorkflowExceptionInServiceException_WhenAosHasBeenIssued_AndRespondentIsRepresentedByNonRegisteredSolicitor()
        throws WorkflowException {
        doThrow(WorkflowException.class).when(aosOfflineTriggerRequestWorkflow).requestAosOfflineToBeTriggered(TEST_CASE_ID);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(Map.of(RESP_SOL_REPRESENTED, YES_VALUE))
            .build();

        CaseOrchestrationServiceException serviceException = assertThrows(CaseOrchestrationServiceException.class,
            () -> classUnderTest.runActionsAfterAosHasBeenIssued(TEST_EVENT_ID, caseDetails));
        assertThat(serviceException.getCaseId().get(), equalTo(TEST_CASE_ID));
        assertThat(serviceException.getCause(), isA(WorkflowException.class));

        verify(sendEmailNotificationWorkflow).run(TEST_EVENT_ID, caseDetails);
        verify(aosOfflineTriggerRequestWorkflow).requestAosOfflineToBeTriggered(TEST_CASE_ID);
    }

    @Test
    public void shouldCallAppropriateWorkflowForTriggeringAosOffline() throws WorkflowException, CaseOrchestrationServiceException {
        classUnderTest.triggerAosOfflineForCase(AUTH_TOKEN, TEST_CASE_ID);

        verify(updateCaseWorkflow).run(eq(emptyMap()), eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(ISSUE_AOS_OFFLINE_RESPONDENT_FROM_AOS_AWAITING));
    }
}