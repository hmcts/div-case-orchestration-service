package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ValidateServiceMethodTaskTest {

    private static final String SOL_SERVICE_METHOD = "SolServiceMethod";
    private static final String PERSONAL_SERVICE = "personalService";
    private ValidateServiceMethodTask validateServiceMethodTask;
    private DefaultTaskContext taskContext;

    @Before
    public void setup() {
        validateServiceMethodTask = new ValidateServiceMethodTask();
        taskContext = new DefaultTaskContext();
    }

    @Test
    public void testExecuteThrowsExceptionIfServiceMethodIsPersonalService() throws TaskException {
        validateServiceMethodTask.execute(taskContext, Collections.emptyMap());
    }

    @Test
    public void testExecuteValidatesServiceMethodDoesntThrowsExceptionIfServiceMethodIsNotPersonalService() throws TaskException {
        Map<String, Object> payload = Collections.singletonMap(SOL_SERVICE_METHOD, "test");
        Map<String, Object> execute = validateServiceMethodTask.execute(taskContext, payload);
        assertThat(execute, is(payload));
    }

    @Test(expected = TaskException.class)
    public void testExecuteValidatesServiceMethodDoesntThrowExceptionIfServiceMethodIsNotPresent() throws TaskException {
        Map<String, Object> payload = Collections.singletonMap(SOL_SERVICE_METHOD, PERSONAL_SERVICE);
        validateServiceMethodTask.execute(taskContext, payload);
    }
}