package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DataExtractionWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionServiceImplTest {

    private static final String TEST_AUTH_TOKEN = "testAuthToken";

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
    public void shouldThrowNewExceptionWhenDataExtractionWorkflowFails() throws WorkflowException {
        doThrow(WorkflowException.class).when(dataExtractionWorkflow).run();

        CaseOrchestrationServiceException exception = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.requestDataExtractionForPreviousDay()
        );
        assertThat(exception.getCause(), is(instanceOf(WorkflowException.class)));
    }

    @Test
    public void shouldCallFamilyManDataExtractionWorkflowWithCorrectParameters() throws WorkflowException, CaseOrchestrationServiceException {
        classUnderTest.extractCasesToFamilyMan(DA, LocalDate.now(), TEST_AUTH_TOKEN);

        verify(mockWorkflow).run(eq(DA), eq(LocalDate.now()), eq(TEST_AUTH_TOKEN));
    }

    @Test
    public void shouldThrowNewExceptionWhenWorkflowFails() throws WorkflowException {
        doThrow(WorkflowException.class).when(mockWorkflow).run(any(), any(), any());

        CaseOrchestrationServiceException exception = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.extractCasesToFamilyMan(DA, LocalDate.now(), TEST_AUTH_TOKEN)
        );
        assertThat(exception.getCause(), is(instanceOf(WorkflowException.class)));
    }
}
