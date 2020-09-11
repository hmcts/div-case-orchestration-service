package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddCourtsToPayloadTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveAosCase;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveAosCaseWorkflowUTest {

    @Mock
    private RetrieveAosCase retrieveAosCase;

    @Mock
    private CaseDataToDivorceFormatter caseDataToDivorceFormatter;

    @Mock
    private AddCourtsToPayloadTask addCourtsToPayloadTask;

    @InjectMocks
    private RetrieveAosCaseWorkflow classUnderTest;

    private Task<Map<String, Object>>[] mainTasks;
    private final ImmutablePair<String, Object> authTokenPair = new ImmutablePair<>(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

    @Before
    public void setUp() {
        mainTasks = new Task[] {
            retrieveAosCase,
            caseDataToDivorceFormatter
        };
    }

    @Test
    public void whenRetrieveAos_thenProcessAsExpected() throws WorkflowException, TaskException {
        Map<String, Object> retrievedCaseData = singletonMap("retrievedKey", "retrievedValue");
        final CaseDataResponse caseDataResponse = CaseDataResponse.builder().data(retrievedCaseData).build();
        when(classUnderTest.execute(mainTasks, null, authTokenPair)).thenReturn(caseDataResponse);
        when(addCourtsToPayloadTask.execute(any(), eq(retrievedCaseData))).thenReturn(singletonMap("modifiedKey", "modifiedValue"));

        CaseDataResponse returnedCaseDataResponse = classUnderTest.run(AUTH_TOKEN);

        assertThat(returnedCaseDataResponse.getData(), hasEntry("modifiedKey", "modifiedValue"));
        verify(addCourtsToPayloadTask).execute(any(), eq(retrievedCaseData));
    }

    @Test
    public void shouldThrowWorkflowException_whenAddCourtTaskThrowsTaskException() throws WorkflowException, TaskException {
        Map<String, Object> retrievedCaseData = singletonMap("retrievedKey", "retrievedValue");
        final CaseDataResponse caseDataResponse = CaseDataResponse.builder().data(retrievedCaseData).build();
        when(classUnderTest.execute(mainTasks, null, authTokenPair)).thenReturn(caseDataResponse);
        when(addCourtsToPayloadTask.execute(any(), eq(retrievedCaseData))).thenThrow(TaskException.class);

        WorkflowException exception = assertThrows(
            WorkflowException.class,
            () -> classUnderTest.run(AUTH_TOKEN)
        );

        assertThat(exception.getCause(), is(instanceOf(TaskException.class)));
    }
}
