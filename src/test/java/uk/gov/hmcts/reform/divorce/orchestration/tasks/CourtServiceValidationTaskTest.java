package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.SOL_SERVICE_METHOD_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_SERVICE_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class CourtServiceValidationTaskTest {

    private CourtServiceValidationTask courtServiceValidationTask;
    private DefaultTaskContext taskContext;

    @Before
    public void setup() {
        courtServiceValidationTask = new CourtServiceValidationTask();
        taskContext = new DefaultTaskContext();
    }

    @Test
    public void testExecuteValidatesServiceMethodHappyCase() throws TaskException {
        Map<String, Object> payload = Collections.singletonMap(
                SOL_SERVICE_METHOD_CCD_FIELD, COURT_SERVICE_VALUE
        );

        Map<String, Object> execute = courtServiceValidationTask.execute(taskContext, payload);

        assertThat(execute, is(payload));
    }

    @Test(expected = TaskException.class)
    public void testExecuteValidatesServiceMethodThrowsExceptionIfServiceMethodIsNotPresent() throws TaskException {
        courtServiceValidationTask.execute(taskContext, Collections.emptyMap());
    }

    @Test(expected = TaskException.class)
    public void testExecuteValidatesServiceMethodThrowsExceptionIfServiceMethodIsNotExpected() throws TaskException {
        Map<String, Object> payload = Collections.singletonMap(
                SOL_SERVICE_METHOD_CCD_FIELD, "test"
        );
        Map<String, Object> execute = courtServiceValidationTask.execute(taskContext, payload);
        assertThat(execute, is(payload));
    }
}