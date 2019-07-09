package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SearchDNPronouncedCases;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDNPronouncedCase;

import static org.junit.Assert.assertEquals;
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

    private static String DAYS_BEFORE_ELIGBLE_FOR_DA = "43d";
    private static int EXPECTED_CASES_PROCESSED_COUNT = 10;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private SearchDNPronouncedCases searchDNPronouncedCases;

    @Mock
    private UpdateDNPronouncedCase updateDNPronouncedCase;

    @InjectMocks
    private UpdateDNPronouncedCasesWorkflow classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "awaitingDAPeriod", DAYS_BEFORE_ELIGBLE_FOR_DA);
    }

    @Test
    public void execute_taskExceptionThrownInAnyTask_workflowExceptionThrown() throws TaskException, WorkflowException {

        expectedException.expect(WorkflowException.class);
        expectedException.expectMessage("a WorfklowException message");

        when(searchDNPronouncedCases.execute(any(), any())).thenThrow(new TaskException("a WorfklowException message"));
        classUnderTest.run(AUTH_TOKEN);

        verify(searchDNPronouncedCases, times(1)).execute(any(), any());
        verify(updateDNPronouncedCase, times(0)).execute(any(), any());
    }

    @Test
    public void execute_10CasesEligibleForDA_10CasesProcessed() throws TaskException, WorkflowException {
        whenSearchTaskSetCasesCountInContext();
        whenUpdateTaskGetCasesCountAndPutInContext();

        int actualCasesProcessedCount = classUnderTest.run(AUTH_TOKEN);

        verify(searchDNPronouncedCases, times(1)).execute(any(), any());
        verify(updateDNPronouncedCase, times(1)).execute(any(), any());
        assertEquals(actualCasesProcessedCount, EXPECTED_CASES_PROCESSED_COUNT);
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
        }).when(searchDNPronouncedCases).execute(any(), any());
    }

    private void whenUpdateTaskGetCasesCountAndPutInContext() throws TaskException {
        doAnswer(args -> {
            int casesCount = getSearchResult(args.getArgument(0));
            updateCasesProcessedCount(args.getArgument(0), casesCount);
            return null;
        }).when(updateDNPronouncedCase).execute(any(), any());
    }
}
