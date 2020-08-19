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
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ReceivedServiceAddedDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceDecisionMadeWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceDecisionMakingWorkflow;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class ServiceJourneyServiceImplTest {

    @Mock
    private MakeServiceDecisionDateWorkflow makeServiceDecisionDateWorkflow;

    @Mock
    private ReceivedServiceAddedDateWorkflow receivedServiceAddedDateWorkflow;

    @Mock
    private ServiceDecisionMadeWorkflow serviceDecisionMadeWorkflow;

    @Mock
    private ServiceDecisionMakingWorkflow serviceDecisionMakingWorkflow;

    @InjectMocks
    private ServiceJourneyServiceImpl classUnderTest;

    @Test
    public void whenServiceApplicationIsGrantedThenReturnServiceApplicationNotApproved()
        throws ServiceJourneyServiceException, WorkflowException {
        runTestMakeServiceDecision(NO_VALUE, SERVICE_APPLICATION_NOT_APPROVED);
    }

    @Test
    public void whenServiceApplicationNotGrantedThenReturnAwaitingDNApplication()
        throws ServiceJourneyServiceException, WorkflowException {
        runTestMakeServiceDecision(YES_VALUE, AWAITING_DECREE_NISI);
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
    public void serviceApplicationGrantedShouldThrowServiceJourneyServiceException()
        throws ServiceJourneyServiceException, WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(ImmutableMap.of(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE))
            .build();

        when(makeServiceDecisionDateWorkflow.run(any(CaseDetails.class), anyString()))
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

    private CcdCallbackRequest buildCcdCallbackRequest() {
        return CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("21431").build())
            .build();
    }

    private void runTestMakeServiceDecision(String decision, String expectedState)
        throws ServiceJourneyServiceException, WorkflowException {
        Map<String, Object> payload = ImmutableMap.of(CcdFields.SERVICE_APPLICATION_GRANTED, decision);
        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).build();

        when(makeServiceDecisionDateWorkflow.run(caseDetails, AUTH_TOKEN)).thenReturn(payload);

        CcdCallbackResponse response = classUnderTest.makeServiceDecision(caseDetails, AUTH_TOKEN);

        assertThat(response.getState(), is(expectedState));
    }
}
