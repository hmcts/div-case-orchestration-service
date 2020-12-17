package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.AlternativeServiceConfirmedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.AosNotReceivedForProcessServerWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.ConfirmAlternativeServiceWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.ConfirmProcessServerServiceWorkflow;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_CASE_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PAYLOAD_TO_RETURN;

@RunWith(MockitoJUnitRunner.class)
public class AlternativeServiceServiceImplTest {

    @Mock
    private ConfirmAlternativeServiceWorkflow confirmAlternativeServiceWorkflow;

    @Mock
    private ConfirmProcessServerServiceWorkflow confirmProcessServerServiceWorkflow;

    @Mock
    private AosNotReceivedForProcessServerWorkflow aosNotReceivedForProcessServerWorkflow;

    @Mock
    private AlternativeServiceConfirmedWorkflow alternativeServiceConfirmedWorkflow;

    @InjectMocks
    private AlternativeServiceServiceImpl alternativeServiceService;

    @Test
    public void whenConfirmAlternativeServiceThenConfirmAlternativeServiceWorkflowIsCalled()
        throws CaseOrchestrationServiceException, WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build();

        alternativeServiceService.confirmAlternativeService(caseDetails);

        verify(confirmAlternativeServiceWorkflow).run(caseDetails);
    }

    @Test
    public void whenConfirmAlternativeServiceWorkflowThrowsWorkflowException_thenRethrowServiceException()
        throws WorkflowException {
        when(confirmAlternativeServiceWorkflow.run(any())).thenThrow(WorkflowException.class);

        assertThrows(
            CaseOrchestrationServiceException.class,
            () -> alternativeServiceService.confirmAlternativeService(CaseDetails.builder().build())
        );
    }

    @Test
    public void whenConfirmProcessServerServiceThenConfirmAlternativeServiceWorkflowIsCalled()
        throws CaseOrchestrationServiceException, WorkflowException {

        when(confirmProcessServerServiceWorkflow.run(any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        CaseDetails returnedCaseDetails = alternativeServiceService.confirmProcessServerService(TEST_INCOMING_CASE_DETAILS);

        assertThat(returnedCaseDetails.getCaseData(), is(TEST_PAYLOAD_TO_RETURN));
        verify(confirmProcessServerServiceWorkflow).run(TEST_INCOMING_CASE_DETAILS);
    }

    @Test
    public void whenConfirmProcessServerServiceWorkflowThrowsWorkflowException_thenThrowServiceException()
        throws WorkflowException {
        when(confirmProcessServerServiceWorkflow.run(any())).thenThrow(WorkflowException.class);

        assertThrows(
            CaseOrchestrationServiceException.class,
            () -> alternativeServiceService.confirmProcessServerService(CaseDetails.builder().build())
        );
    }

    @Test
    public void whenAosNotReceivedForProcessServer_thenAosNotReceivedForProcessServerWorkflowIsCalled()
        throws CaseOrchestrationServiceException, WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build();

        alternativeServiceService.aosNotReceivedForProcessServer(caseDetails);

        verify(aosNotReceivedForProcessServerWorkflow).run(caseDetails);
    }

    @Test
    public void whenAosNotReceivedForProcessServerThrowsWorkflowException_thenRethrowServiceException()
        throws WorkflowException {
        when(aosNotReceivedForProcessServerWorkflow.run(any())).thenThrow(WorkflowException.class);

        assertThrows(
            CaseOrchestrationServiceException.class,
            () -> alternativeServiceService.aosNotReceivedForProcessServer(CaseDetails.builder().build())
        );
    }

    @Test
    public void whenAlternativeServiceConfirmedThenAlternativeServiceConfirmedWorkflowIsCalled()
        throws CaseOrchestrationServiceException, WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build();

        alternativeServiceService.alternativeServiceConfirmed(caseDetails);

        verify(alternativeServiceConfirmedWorkflow).run(caseDetails);
    }

    @Test
    public void whenAlternativeServiceConfirmedThrowsWorkflowExceptionThenRethrowServiceException()
        throws WorkflowException {
        when(alternativeServiceConfirmedWorkflow.run(any())).thenThrow(WorkflowException.class);

        assertThrows(
            CaseOrchestrationServiceException.class,
            () -> alternativeServiceService.alternativeServiceConfirmed(CaseDetails.builder().build())
        );
    }
}
