package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction.DataExtractionTask;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionWorkflowTest {

    @Mock
    private DataExtractionTask dataExtractionTask;

    @InjectMocks
    private DataExtractionWorkflow classUnderTest;

    @Test
    public void run_shouldExecuteTask() throws TaskException, WorkflowException {
        classUnderTest.run();
        verify(dataExtractionTask, times(1)).execute(any(), any());
    }
}
