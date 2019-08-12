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

@RunWith(MockitoJUnitRunner.class)
public class ServiceMethodValidationTaskTest {

    private static final String SOL_SERVICE_METHOD = "SolServiceMethod";
    private static final String PERSONAL_SERVICE = "personalService";
    private DefaultTaskContext taskContext;
    private ServiceMethodValidationTask serviceMethodValidationTask;

    @Before
    public void setup() {
        serviceMethodValidationTask = new ServiceMethodValidationTask();
        taskContext = new DefaultTaskContext();
    }

    @Test
    public void testExecuteThrowsExceptionIfServiceMethodIsPersonalService() throws TaskException {
        serviceMethodValidationTask.execute(taskContext, Collections.emptyMap());
    }

    @Test
    public void testExecuteValidatesServiceMethodDoesntThrowsExceptionIfServiceMethodIsNotPersonalService() throws TaskException {
        Map<String, Object> payload = Collections.singletonMap(SOL_SERVICE_METHOD, "test");
        Map<String, Object> execute = serviceMethodValidationTask.execute(taskContext, payload);
        assertThat(execute, is(payload));
    }

    @Test(expected = TaskException.class)
    public void testExecuteValidatesServiceMethodDoesntThrowExceptionIfServiceMethodIsNotPresent() throws TaskException {
        Map<String, Object> payload = Collections.singletonMap(SOL_SERVICE_METHOD, PERSONAL_SERVICE);
        serviceMethodValidationTask.execute(taskContext, payload);
    }
}