package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DataExtractionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow;

import java.time.LocalDate;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionServiceImplTest {

    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private DataExtractionWorkflow dataExtractionWorkflow;

    @Mock
    private FamilyManDataExtractionWorkflow mockWorkflow;

    @InjectMocks
    private DataExtractionServiceImpl classUnderTest;

    @Test
    public void shouldCallDataExtractionWorkflowWithCorrectParameters() throws WorkflowException, CaseOrchestrationServiceException {
        classUnderTest.requestDataExtractionForPreviousDay();
        verify(dataExtractionWorkflow).run();
    }

    @Test
    public void shouldThrowNewExceptionWhenDataExtractionWorkflowFails() throws WorkflowException, CaseOrchestrationServiceException {
        doThrow(WorkflowException.class).when(dataExtractionWorkflow).run();
        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectCause(instanceOf(WorkflowException.class));

        classUnderTest.requestDataExtractionForPreviousDay();
    }

    @Test
    public void shouldCallFamilyManDataExtractionWorkflowWithCorrectParameters() throws WorkflowException, CaseOrchestrationServiceException {
        classUnderTest.extractCasesToFamilyMan(DA, LocalDate.now(), TEST_AUTH_TOKEN);

        verify(mockWorkflow).run(eq(DA), eq(LocalDate.now()), eq(TEST_AUTH_TOKEN));
    }

    @Test
    public void shouldThrowNewExceptionWhenWorkflowFails() throws WorkflowException, CaseOrchestrationServiceException {
        doThrow(WorkflowException.class).when(mockWorkflow).run(any(), any(), any());
        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectCause(instanceOf(WorkflowException.class));

        classUnderTest.extractCasesToFamilyMan(DA, LocalDate.now(), TEST_AUTH_TOKEN);
    }

}
