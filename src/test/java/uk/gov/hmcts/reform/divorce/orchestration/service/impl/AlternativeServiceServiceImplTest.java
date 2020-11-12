package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.ConfirmAlternativeServiceWorkflow;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class AlternativeServiceServiceImplTest {

    @Mock
    private ConfirmAlternativeServiceWorkflow confirmAlternativeServiceWorkflow;

    @InjectMocks
    private AlternativeServiceServiceImpl alternativeServiceService;

    @Test
    public void whenConfirmAlternativeService_thenConfirmAlternativeServiceWorkflowIsCalled()
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
    public void whenConfirmProcessServer_thenConfirmAlternativeServiceWorkflowIsCalled()
        throws CaseOrchestrationServiceException, WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build();

        alternativeServiceService.confirmProcessServer(caseDetails);

        verify(confirmAlternativeServiceWorkflow).run(caseDetails);
    }
}
