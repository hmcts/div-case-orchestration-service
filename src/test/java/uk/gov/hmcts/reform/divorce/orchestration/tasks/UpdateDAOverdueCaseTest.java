package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASES_OVERDUE_FOR_DA_PROCESSED_COUNT;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDAOverdueCaseTest {

    @Mock
    private UpdateCaseInCCD updateCaseInCCD;

    @InjectMocks
    private UpdateDAOverdueCase classToTest;

    @Before
    public void setUp() {
        when(updateCaseInCCD.execute(any(), any())).thenReturn(Collections.emptyMap());
    }

    @Test
    public void givenListOfTwoCases_thenProcessCasesTwoTimes() throws TaskException {
        TaskContext context = new DefaultTaskContext();
        String[] caseIds = {"someId1", "someId2"};
        context.setTransientObject(SEARCH_RESULT_KEY, Arrays.asList(caseIds));
        Map<String, Object> payload = new HashMap<>();

        try {
            Map<String, Object> result = classToTest.execute(context, payload);
        } catch (TaskException e) {
            fail();
        }

        assertEquals(2, (int) context.getTransientObject(CASES_OVERDUE_FOR_DA_PROCESSED_COUNT));
        verify(updateCaseInCCD, times(2)).execute(any(),anyMap());
    }

    @Test
    public void givenEmptyListOfCases_whenUpdateCases_thenReturnNoCasesProcessed() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(SEARCH_RESULT_KEY, Collections.emptyList());
        Map<String, Object> payload = new HashMap<>();

        try {
            Map<String, Object> returnedContext = classToTest.execute(context, payload);
            assertEquals(0, (int) context.getTransientObject(CASES_OVERDUE_FOR_DA_PROCESSED_COUNT));
        } catch (TaskException e) {
            fail();
        }
    }
}
