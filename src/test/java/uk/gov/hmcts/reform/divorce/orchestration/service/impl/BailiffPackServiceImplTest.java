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
import uk.gov.hmcts.reform.divorce.orchestration.workflows.bailiff.IssueBailiffPackWorkflow;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_PAYLOAD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PAYLOAD_TO_RETURN;
import static uk.gov.hmcts.reform.divorce.orchestration.controller.util.CallbackControllerTestUtils.assertCaseOrchestrationServiceExceptionIsSetProperly;

@RunWith(MockitoJUnitRunner.class)
public class BailiffPackServiceImplTest {

    @Mock
    IssueBailiffPackWorkflow issueBailiffPackWorkflow;

    @InjectMocks
    private BailiffPackServiceImpl bailiffPackServiceImpl;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(TEST_INCOMING_PAYLOAD).build();
    }

    @Test
    public void shouldCallWorkflow_WhenPreparingAosNotReceivedEventForSubmission() throws WorkflowException, CaseOrchestrationServiceException {
        when(issueBailiffPackWorkflow.issueCertificateOfServiceDocument(any(), any(), any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        Map<String, Object> returnedCaseData = bailiffPackServiceImpl.issueCertificateOfServiceDocument(AUTH_TOKEN, caseDetails);

        assertThat(returnedCaseData, equalTo(TEST_PAYLOAD_TO_RETURN));
        verify(issueBailiffPackWorkflow).issueCertificateOfServiceDocument(AUTH_TOKEN, TEST_CASE_ID, TEST_INCOMING_PAYLOAD);
    }

    @Test
    public void shouldThrowAppropriateException_WhenCatchingWorkflowException_PreparingAosNotReceivedEventForSubmission() throws WorkflowException {
        when(issueBailiffPackWorkflow.issueCertificateOfServiceDocument(any(), any(), any())).thenThrow(WorkflowException.class);

        try {
            bailiffPackServiceImpl.issueCertificateOfServiceDocument(AUTH_TOKEN, caseDetails);
            fail("Should have thrown exception");
        } catch (CaseOrchestrationServiceException exception) {
            assertCaseOrchestrationServiceExceptionIsSetProperly(exception);
        }
    }
}
