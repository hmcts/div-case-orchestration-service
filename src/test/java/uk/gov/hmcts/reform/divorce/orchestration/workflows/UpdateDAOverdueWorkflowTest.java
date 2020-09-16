package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SearchCasesDAOverdueTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDAOverdueCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASES_OVERDUE_FOR_DA_PROCESSED_COUNT;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDAOverdueWorkflowTest {

    private static final int EXPECTED_CASES_PROCESSED_COUNT = 10;

    @Mock
    private SearchCasesDAOverdueTask searchCasesDAOverdueTask;

    @Mock
    private UpdateDAOverdueCase updateDAOverdueCase;

    @InjectMocks
    private UpdateDAOverdueWorkflow classUnderTest;

    @Test
    public void execute_taskExceptionThrownInAnyTask_workflowExceptionThrown() throws TaskException, WorkflowException {
        when(searchCasesDAOverdueTask.execute(any(), any())).thenThrow(new TaskException("a WorkflowException message"));

        WorkflowException exception = assertThrows(
            WorkflowException.class,
            () -> classUnderTest.run(AUTH_TOKEN)
        );

        assertThat(exception.getMessage(), is("a WorkflowException message"));

        verify(searchCasesDAOverdueTask, times(1)).execute(any(), any());
        verify(updateDAOverdueCase, times(0)).execute(any(), any());
    }

    @Test
    public void execute_10CasesEligibleForDA_10CasesProcessed() throws TaskException, WorkflowException {
        whenSearchTaskSetCasesCountInContext();
        whenUpdateTaskGetCasesCountAndPutInContext();

        int actualCasesProcessedCount = classUnderTest.run(AUTH_TOKEN);

        verify(searchCasesDAOverdueTask, times(1)).execute(any(), any());
        verify(updateDAOverdueCase, times(1)).execute(any(), any());
        assertEquals(EXPECTED_CASES_PROCESSED_COUNT, actualCasesProcessedCount);
    }

    private int getSearchResult(TaskContext taskContext) {
        return taskContext.getTransientObject(SEARCH_RESULT_KEY);
    }

    private void setSearchResult(TaskContext taskContext, int count) {
        taskContext.setTransientObject(SEARCH_RESULT_KEY, count);
    }

    private void updateCasesProcessedCount(TaskContext taskContext, int count) {
        taskContext.setTransientObject(CASES_OVERDUE_FOR_DA_PROCESSED_COUNT, count);
    }

    private void whenSearchTaskSetCasesCountInContext() throws TaskException {
        doAnswer(args -> {
            setSearchResult(args.getArgument(0), EXPECTED_CASES_PROCESSED_COUNT);
            return null;
        }).when(searchCasesDAOverdueTask).execute(any(), any());
    }

    private void whenUpdateTaskGetCasesCountAndPutInContext() throws TaskException {
        doAnswer(args -> {
            int casesCount = getSearchResult(args.getArgument(0));
            updateCasesProcessedCount(args.getArgument(0), casesCount);
            return null;
        }).when(updateDAOverdueCase).execute(any(), any());
    }
}
