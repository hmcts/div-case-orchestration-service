package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ReceivedServiceAddedDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceRefusalOrderWorkflow;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderTask.FINAL_DECISION;

@RunWith(MockitoJUnitRunner.class)
public class ServiceJourneyServiceImplTest {

    @Mock
    private MakeServiceDecisionDateWorkflow makeServiceDecisionDateWorkflow;

    @Mock
    private ReceivedServiceAddedDateWorkflow receivedServiceAddedDateWorkflow;

    @Mock
    private ServiceRefusalOrderWorkflow serviceRefusalOrderWorkflow;

    @InjectMocks
    private ServiceJourneyServiceImpl classUnderTest;

    @Test
    public void whenServiceApplicationIsGrantedThenReturnServiceApplicationNotApproved() throws WorkflowException {
        runTestMakeServiceDecision(NO_VALUE, SERVICE_APPLICATION_NOT_APPROVED);
    }

    @Test
    public void whenServiceApplicationNotGrantedThenReturnAwaitingDNApplication() throws WorkflowException {
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

        when(serviceRefusalOrderWorkflow.run(any(), anyString(), anyString())).thenReturn(caseDetails.getCaseDetails().getCaseData());

        CcdCallbackResponse response = classUnderTest.serviceDecisionMade(caseDetails.getCaseDetails(), AUTH_TOKEN, FINAL_DECISION);

        assertThat(response.getData(), is(caseDetails.getCaseDetails().getCaseData()));

        verify(serviceRefusalOrderWorkflow).run(eq(caseDetails.getCaseDetails()), eq(FINAL_DECISION), eq(AUTH_TOKEN));
    }

    @Test
    public void receivedServiceAddedDateShouldCallWorkflow() throws Exception {
        CcdCallbackRequest input = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("21431").build())
            .build();

        classUnderTest.receivedServiceAddedDate(input);

        verify(receivedServiceAddedDateWorkflow).run(input.getCaseDetails());
    }

    protected void runTestMakeServiceDecision(String decision, String expectedState)
        throws WorkflowException {
        Map<String, Object> payload = ImmutableMap.of(SERVICE_APPLICATION_GRANTED, decision);
        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).build();

        when(makeServiceDecisionDateWorkflow.run(caseDetails, AUTH_TOKEN)).thenReturn(payload);

        CcdCallbackResponse response = classUnderTest.makeServiceDecision(caseDetails, AUTH_TOKEN);

        assertThat(response.getState(), is(expectedState));
    }
}
