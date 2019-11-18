package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddCourtsToPayloadTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCase;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class GetCaseWorkflowTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private GetCase getCase;

    @Mock
    private CaseDataToDivorceFormatterTask caseDataToDivorceFormatter;

    @Mock
    private AddCourtsToPayloadTask addCourtsToPayloadTask;

    @InjectMocks
    private GetCaseWorkflow classUnderTest;

    private Task[] mainTasks;
    private ImmutablePair<String, Object> testAuthTokenPair = new ImmutablePair<>(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

    @Before
    public void setUp() throws Exception {
        mainTasks = new Task[]{
            getCase,
            caseDataToDivorceFormatter
        };
    }

    @Test
    public void whenGetCase_thenProcessAsExpected() throws WorkflowException, TaskException {
        Map<String, Object> fetchedCaseData = singletonMap("fetchedKey", "fetchedValue");
        when(classUnderTest.execute(mainTasks, null, testAuthTokenPair)).thenReturn(CaseDataResponse.builder().data(fetchedCaseData).build());
        when(addCourtsToPayloadTask.execute(any(), eq(fetchedCaseData))).thenReturn(singletonMap("modifiedKey", "modifiedValue"));

        CaseDataResponse returnedCaseResponse = classUnderTest.run(AUTH_TOKEN);

        assertThat(returnedCaseResponse.getData(), hasEntry("modifiedKey", "modifiedValue"));
        verify(addCourtsToPayloadTask).execute(any(), eq(fetchedCaseData));
    }

    @Test
    public void shouldThrowWorkflowException_whenAddCourtTaskThrowsTaskException() throws WorkflowException, TaskException {
        expectedException.expect(WorkflowException.class);
        expectedException.expectCause(instanceOf(TaskException.class));

        Map<String, Object> fetchedCaseData = singletonMap("fetchedKey", "fetchedValue");
        when(classUnderTest.execute(mainTasks, null, testAuthTokenPair)).thenReturn(CaseDataResponse.builder().data(fetchedCaseData).build());
        when(addCourtsToPayloadTask.execute(any(), eq(fetchedCaseData))).thenThrow(TaskException.class);

        classUnderTest.run(AUTH_TOKEN);
    }

}