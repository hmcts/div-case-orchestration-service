package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
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
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionDateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ReceivedServiceAddedDateWorkflow;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DN_APPLICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class ServiceJourneyServiceImplTest extends TestCase {

    @Mock
    private MakeServiceDecisionDateWorkflow makeServiceDecisionDateWorkflow;

    @Mock
    private ReceivedServiceAddedDateWorkflow receivedServiceAddedDateWorkflow;

    @InjectMocks
    private ServiceJourneyServiceImpl classUnderTest;

    @Test
    public void whenServiceApplicationIsGrantedThenReturnServiceApplicationNotApproved() throws WorkflowException {
        runTestMakeServiceDecision(NO_VALUE, SERVICE_APPLICATION_NOT_APPROVED);
    }

    @Test
    public void whenServiceApplicationNotGrantedThenReturnAwaitingDNApplication() throws WorkflowException {
        runTestMakeServiceDecision(YES_VALUE, AWAITING_DN_APPLICATION);
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
        Map<String, Object> payload = ImmutableMap.of(CcdFields.SERVICE_APPLICATION_GRANTED, decision);
        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).build();

        when(makeServiceDecisionDateWorkflow.run(caseDetails)).thenReturn(payload);

        CcdCallbackResponse response = classUnderTest.makeServiceDecision(caseDetails);

        assertThat(response.getState(), is(expectedState));
    }
}
