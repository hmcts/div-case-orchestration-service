package uk.gov.hmcts.reform.divorce.orchestration.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CreateBulkCaseJobTest {

    @Mock
    private CaseOrchestrationService orchestrationServiceMock;

    @Mock
    private JobExecutionContext jobExecutionContextMock;

    @InjectMocks
    private CreateBulkCaseJob classToTest;

    @Test
    public void whenExecuteJob_thenGenerateBulkCaseIsExecuted() throws JobExecutionException, WorkflowException {

        classToTest.execute(jobExecutionContextMock);
        verify(orchestrationServiceMock, times(1)).generateBulkCaseForListing();
    }

    @Test(expected = JobExecutionException.class)
    public void givenException_whenExecuteJob_thenPropagateJobExecutionException() throws JobExecutionException, WorkflowException {
        doThrow(new WorkflowException("Workflow error")).when(orchestrationServiceMock).generateBulkCaseForListing();

        classToTest.execute(jobExecutionContextMock);
    }
}
