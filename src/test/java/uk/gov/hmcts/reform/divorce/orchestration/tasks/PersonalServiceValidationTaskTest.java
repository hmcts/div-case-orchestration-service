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
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.PERSONAL_SERVICE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.SOL_SERVICE_METHOD_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class PersonalServiceValidationTaskTest {

    private PersonalServiceValidationTask personalServiceValidationTask;
    private DefaultTaskContext taskContext;

    @Before
    public void setup() {
        personalServiceValidationTask = new PersonalServiceValidationTask();
        taskContext = new DefaultTaskContext();
    }

    @Test
    public void testExecuteValidatesServiceMethodHappyCase() throws TaskException {
        Map<String, Object> payload = Collections.singletonMap(
                SOL_SERVICE_METHOD_CCD_FIELD, PERSONAL_SERVICE_VALUE
        );

        Map<String, Object> execute = personalServiceValidationTask.execute(taskContext, payload);

        assertThat(execute, is(payload));
    }

    @Test(expected = TaskException.class)
    public void testExecuteValidatesServiceMethodThrowsExceptionIfServiceMethodIsNotPresent() throws TaskException {
        personalServiceValidationTask.execute(taskContext, Collections.emptyMap());
    }

    @Test(expected = TaskException.class)
    public void testExecuteValidatesServiceMethodThrowsExceptionIfServiceMethodIsNotExpected() throws TaskException {
        Map<String, Object> payload = Collections.singletonMap(
                SOL_SERVICE_METHOD_CCD_FIELD, "test"
        );
        Map<String, Object> execute = personalServiceValidationTask.execute(taskContext, payload);
        assertThat(execute, is(payload));
    }
}