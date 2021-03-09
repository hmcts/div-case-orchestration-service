package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.BailiffOutcomeWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.FurtherPaymentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ReceivedServiceAddedDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceDecisionMadeWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceDecisionMakingWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.SetupConfirmServicePaymentWorkflow;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_BAILIFF_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_BAILIFF_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.BAILIFF;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DISPENSED;

@RunWith(MockitoJUnitRunner.class)
public class ServiceJourneyServiceImplTest {

    @Mock
    private MakeServiceDecisionWorkflow makeServiceDecisionWorkflow;

    @Mock
    private ReceivedServiceAddedDateWorkflow receivedServiceAddedDateWorkflow;

    @Mock
    private ServiceDecisionMadeWorkflow serviceDecisionMadeWorkflow;

    @Mock
    private ServiceDecisionMakingWorkflow serviceDecisionMakingWorkflow;

    @Mock
    private SetupConfirmServicePaymentWorkflow setupConfirmServicePaymentWorkflow;

    @Mock
    private FurtherPaymentWorkflow furtherPaymentWorkflow;

    @Mock
    private BailiffOutcomeWorkflow bailiffOutcomeWorkflow;

    @InjectMocks
    private ServiceJourneyServiceImpl classUnderTest;

    @Test
    public void whenServiceApplicationIsNotGrantedThenReturnServiceApplicationNotApproved()
        throws ServiceJourneyServiceException, WorkflowException {
        runTestMakeServiceDecision(NO_VALUE, SERVICE_APPLICATION_NOT_APPROVED, BAILIFF);
    }

    @Test
    public void whenBailiffServiceApplicationGrantedThenReturnAwaitingBailiffService()
        throws ServiceJourneyServiceException, WorkflowException {
        runTestMakeServiceDecision(YES_VALUE, AWAITING_BAILIFF_SERVICE, BAILIFF);
    }

    @Test
    public void whenNonBailiffServiceApplicationGrantedThenReturnAwaitingDNApplication()
        throws ServiceJourneyServiceException, WorkflowException {
        runTestMakeServiceDecision(YES_VALUE, AWAITING_DECREE_NISI, ApplicationServiceTypes.DEEMED);
    }

    @Test
    public void whenServiceDecisionIsMadeThenUpdateServiceApplicationRefusalOrderDocuments() throws Exception {
        CcdCallbackRequest caseDetails = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseId("21431")
                .state(AWAITING_SERVICE_CONSIDERATION)
                .build())
            .build();

        when(serviceDecisionMadeWorkflow.run(any(), anyString()))
            .thenReturn(caseDetails.getCaseDetails().getCaseData());

        CcdCallbackResponse response = classUnderTest.serviceDecisionMade(caseDetails.getCaseDetails(), AUTH_TOKEN);

        assertThat(response.getData(), is(caseDetails.getCaseDetails().getCaseData()));

        verify(serviceDecisionMadeWorkflow).run(eq(caseDetails.getCaseDetails()), eq(AUTH_TOKEN));
    }

    @Test(expected = ServiceJourneyServiceException.class)
    public void serviceDecisionMadeThenThrowServiceJourneyServiceException() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(serviceDecisionMadeWorkflow.run(any(), anyString())).thenThrow(WorkflowException.class);

        classUnderTest.serviceDecisionMade(caseDetails, AUTH_TOKEN);
    }

    @Test(expected = ServiceJourneyServiceException.class)
    public void serviceApplicationGrantedShouldThrowServiceJourneyServiceException()
        throws ServiceJourneyServiceException, WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(ImmutableMap.of(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE))
            .build();

        when(makeServiceDecisionWorkflow.run(any(CaseDetails.class), anyString()))
            .thenThrow(WorkflowException.class);

        classUnderTest.makeServiceDecision(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void receivedServiceAddedDateShouldCallWorkflow()
        throws ServiceJourneyServiceException, WorkflowException {
        CcdCallbackRequest input = buildCcdCallbackRequest();

        classUnderTest.receivedServiceAddedDate(input);

        verify(receivedServiceAddedDateWorkflow).run(input.getCaseDetails());
    }

    @Test(expected = ServiceJourneyServiceException.class)
    public void receivedServiceAddedDateShouldThrowServiceJourneyServiceException()
        throws ServiceJourneyServiceException, WorkflowException {
        CcdCallbackRequest input = buildCcdCallbackRequest();

        when(receivedServiceAddedDateWorkflow.run(any(CaseDetails.class))).thenThrow(WorkflowException.class);

        classUnderTest.receivedServiceAddedDate(input);
    }

    @Test
    public void serviceDecisionRefusalShouldCallWorkflow()
        throws ServiceJourneyServiceException, WorkflowException {
        CcdCallbackRequest input = buildCcdCallbackRequest();

        classUnderTest.serviceDecisionRefusal(input.getCaseDetails(), AUTH_TOKEN);

        verify(serviceDecisionMakingWorkflow).run(input.getCaseDetails(), AUTH_TOKEN);
    }

    @Test(expected = ServiceJourneyServiceException.class)
    public void serviceDecisionRefusalShouldThrowServiceJourneyServiceException()
        throws ServiceJourneyServiceException, WorkflowException {
        CcdCallbackRequest input = buildCcdCallbackRequest();

        when(serviceDecisionMakingWorkflow.run(any(CaseDetails.class), anyString()))
            .thenThrow(WorkflowException.class);

        classUnderTest.serviceDecisionRefusal(input.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void addBailiffReturnShouldCallWorkflow()
            throws ServiceJourneyServiceException, WorkflowException {
        CcdCallbackRequest input = buildCcdCallbackRequest();

        classUnderTest.setupAddBailiffReturnEvent(input.getCaseDetails(), AUTH_TOKEN);

        verify(bailiffOutcomeWorkflow).run(input.getCaseDetails(), AUTH_TOKEN);
    }

    @Test(expected = ServiceJourneyServiceException.class)
    public void addBailiffReturnShouldThrowServiceJourneyServiceException()
            throws ServiceJourneyServiceException, WorkflowException {
        CcdCallbackRequest input = buildCcdCallbackRequest();

        when(bailiffOutcomeWorkflow.run(any(CaseDetails.class), anyString()))
                .thenThrow(WorkflowException.class);

        classUnderTest.setupAddBailiffReturnEvent(input.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void givenCaseData_whenSetupConfirmServicePaymentEvent_thenReturnPayload() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

        when(setupConfirmServicePaymentWorkflow.run(eq(caseDetails))).thenReturn(new HashMap<>());

        classUnderTest.setupConfirmServicePaymentEvent(caseDetails);

        verify(setupConfirmServicePaymentWorkflow).run(eq(caseDetails));
    }

    @Test
    public void givenCaseData_whenConfirmServicePaymentEvent_thenReturnPayload() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_TYPE, TEST_SERVICE_APPLICATION_TYPE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        when(furtherPaymentWorkflow.run(eq(caseDetails), anyString())).thenReturn(new HashMap<>());

        classUnderTest.confirmServicePaymentEvent(caseDetails, AUTH_TOKEN);

        verify(furtherPaymentWorkflow).run(eq(caseDetails), anyString());
    }

    @Test
    public void shouldChangeToAwaitingServiceConsiderationState_whenDispensedApplicationIsPaid() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_TYPE, DISPENSED);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();

        CcdCallbackResponse response = classUnderTest.confirmServicePaymentEvent(caseDetails, AUTH_TOKEN);

        assertThat(response.getState(), is(AWAITING_SERVICE_CONSIDERATION));
    }

    @Test
    public void shouldChangeToAwaitingServiceConsiderationState_whenDeemedApplicationIsPaid() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_TYPE, DEEMED);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();

        CcdCallbackResponse response = classUnderTest.confirmServicePaymentEvent(caseDetails, AUTH_TOKEN);

        assertThat(response.getState(), is(AWAITING_SERVICE_CONSIDERATION));
    }

    @Test
    public void shouldChangeToAwaitingBailiffReferralState_whenBailiffApplicationIsPaid() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_TYPE, BAILIFF);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();

        CcdCallbackResponse response = classUnderTest.confirmServicePaymentEvent(caseDetails, AUTH_TOKEN);

        assertThat(response.getState(), is(AWAITING_BAILIFF_REFERRAL));
    }

    @Test(expected = ServiceJourneyServiceException.class)
    public void shouldThrowException_whenSetupConfirmServicePaymentEventFeeWorkflow_throwsWorkflowException() throws Exception {
        when(setupConfirmServicePaymentWorkflow.run(any())).thenThrow(WorkflowException.class);

        classUnderTest.setupConfirmServicePaymentEvent(CaseDetails.builder().caseId(TEST_CASE_ID).build());
    }

    private CcdCallbackRequest buildCcdCallbackRequest() {
        return CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("21431").build())
            .build();
    }

    private void runTestMakeServiceDecision(String decision, String expectedState, String applicationType)
        throws ServiceJourneyServiceException, WorkflowException {
        Map<String, Object> payload = ImmutableMap.of(
            CcdFields.SERVICE_APPLICATION_GRANTED, decision,
            CcdFields.SERVICE_APPLICATION_TYPE, applicationType);

        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).build();

        when(makeServiceDecisionWorkflow.run(caseDetails, AUTH_TOKEN)).thenReturn(payload);

        CcdCallbackResponse response = classUnderTest.makeServiceDecision(caseDetails, AUTH_TOKEN);

        assertThat(response.getState(), is(expectedState));
    }
}
