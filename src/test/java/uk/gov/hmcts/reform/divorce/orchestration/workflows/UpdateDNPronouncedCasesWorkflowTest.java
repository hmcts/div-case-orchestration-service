package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SearchDNPronouncedCasesTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDNPronouncedCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDNPronouncedCasesWorkflowTest {

    private static final int EXPECTED_CASES_PROCESSED_COUNT = 10;

    @Mock
    private SearchDNPronouncedCasesTask searchDNPronouncedCasesTask;

    @Mock
    private UpdateDNPronouncedCase updateDNPronouncedCase;

    @InjectMocks
    private UpdateDNPronouncedCasesWorkflow classUnderTest;

    @Test
    public void execute_taskExceptionThrownInAnyTask_workflowExceptionThrown() throws TaskException, WorkflowException {
        when(searchDNPronouncedCasesTask.execute(any(), any())).thenThrow(new TaskException("a WorfklowException message"));

        WorkflowException workflowException = assertThrows(
            WorkflowException.class,
            () -> classUnderTest.run(AUTH_TOKEN)
        );

        assertThat(workflowException.getMessage(), is("a WorfklowException message"));

        verify(searchDNPronouncedCasesTask, times(1)).execute(any(), any());
        verify(updateDNPronouncedCase, times(0)).execute(any(), any());
    }

    @Test
    public void execute_10CasesEligibleForDA_10CasesProcessed() throws TaskException, WorkflowException {
        whenSearchTaskSetCasesCountInContext();
        whenUpdateTaskGetCasesCountAndPutInContext();

        int actualCasesProcessedCount = classUnderTest.run(AUTH_TOKEN);

        verify(searchDNPronouncedCasesTask, times(1)).execute(any(), any());
        verify(updateDNPronouncedCase, times(1)).execute(any(), any());
        assertEquals(EXPECTED_CASES_PROCESSED_COUNT, actualCasesProcessedCount);
    }

    private int getSearchResult(TaskContext taskContext) {
        return taskContext.getTransientObject(SEARCH_RESULT_KEY);
    }

    private void setSearchResult(TaskContext taskContext, int count) {
        taskContext.setTransientObject(SEARCH_RESULT_KEY, count);
    }

    private void updateCasesProcessedCount(TaskContext taskContext, int count) {
        taskContext.setTransientObject(CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT, count);
    }

    private void whenSearchTaskSetCasesCountInContext() throws TaskException {
        doAnswer(args -> {
            setSearchResult(args.getArgument(0), EXPECTED_CASES_PROCESSED_COUNT);
            return null;
        }).when(searchDNPronouncedCasesTask).execute(any(), any());
    }

    private void whenUpdateTaskGetCasesCountAndPutInContext() throws TaskException {
        doAnswer(args -> {
            int casesCount = getSearchResult(args.getArgument(0));
            updateCasesProcessedCount(args.getArgument(0), casesCount);
            return null;
        }).when(updateDNPronouncedCase).execute(any(), any());
    }
}
