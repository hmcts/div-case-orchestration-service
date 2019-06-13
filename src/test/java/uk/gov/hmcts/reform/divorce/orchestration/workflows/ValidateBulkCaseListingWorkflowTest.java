package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateBulkCourtHearingDate;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidateBulkCaseListingWorkflowTest {

    @Mock
    private ValidateBulkCourtHearingDate validateBulkCourtHearingDate;

    @InjectMocks
    private ValidateBulkCaseListingWorkflow classUnderTest;

    @Test
    public void whenValidateBulkCaseListingData_thenProcessAsExpected() throws WorkflowException {
        final Task[] tasks = new Task[]{
            validateBulkCourtHearingDate
        };

        Map<String, Object> expected = Collections.emptyMap();

        when(classUnderTest.execute(tasks, Collections.emptyMap())).thenReturn(expected);

        Map<String, Object> actual = classUnderTest.run(Collections.emptyMap());

        assertEquals(expected, actual);
    }
}
