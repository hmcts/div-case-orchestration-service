package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_CASE_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_PAYLOAD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PAYLOAD_TO_RETURN;

@RunWith(MockitoJUnitRunner.class)
public class StandardisedWorkflowTest {

    @Mock
    private Task<Map<String, Object>> firstTask;

    @Mock
    private Task<Map<String, Object>> secondTask;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @Test
    public void shouldCallAppropriateTasksWithAppropriateContextVariables() throws WorkflowException {
        when(firstTask.execute(any(), any())).thenReturn(TEST_PAYLOAD_TO_RETURN);
        when(secondTask.execute(any(), any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        StandardisedWorkflow classUnderTest = new StandardisedWorkflow() {
            @Override
            protected Task<Map<String, Object>>[] getTasksToExecute(CaseDetails caseDetails) {
                return new Task[] {
                    firstTask,
                    secondTask
                };
            }

            @Override
            protected Pair<String, Object>[] prepareContextVariables(CaseDetails caseDetails, String authToken) {
                return new Pair[] {
                    ImmutablePair.of("name1", "value1"),
                    ImmutablePair.of("name2", "value2")
                };
            }
        };

        Map<String, Object> returnedPayload = classUnderTest.run(TEST_INCOMING_CASE_DETAILS, AUTH_TOKEN);

        assertThat(returnedPayload, is(TEST_PAYLOAD_TO_RETURN));
        verify(firstTask).execute(taskContextArgumentCaptor.capture(), eq(TEST_INCOMING_PAYLOAD));
        verify(secondTask).execute(taskContextArgumentCaptor.capture(), eq(TEST_PAYLOAD_TO_RETURN));
        List<TaskContext> capturedTaskContexts = taskContextArgumentCaptor.getAllValues();
        assertThat(capturedTaskContexts, hasSize(2));
        capturedTaskContexts.forEach(taskContext -> {
            assertThat(taskContext.getTransientObject("name1"), is("value1"));
            assertThat(taskContext.getTransientObject("name2"), is("value2"));
        });
    }

    @Test
    public void shouldRethrowException_InTasksAssembly_AsWorkflowException() {
        RuntimeException exceptionToThrow = new RuntimeException("Failed to get tasks");

        StandardisedWorkflow classUnderTest = new StandardisedWorkflow() {
            @Override
            protected Task<Map<String, Object>>[] getTasksToExecute(CaseDetails caseDetails) {
                throw exceptionToThrow;
            }

            @Override
            protected Pair<String, Object>[] prepareContextVariables(CaseDetails caseDetails, String authToken) {
                return new Pair[0];
            }
        };
        WorkflowException thrownException = assertThrows(WorkflowException.class, () -> classUnderTest.run(TEST_INCOMING_CASE_DETAILS, AUTH_TOKEN));
        assertThat(thrownException.getCause(), is(exceptionToThrow));
    }

    @Test
    public void shouldRethrowException_InPreparingContextVariables_AsWorkflowException() {
        RuntimeException exceptionToThrow = new RuntimeException("Failed to set context variables");

        StandardisedWorkflow classUnderTest = new StandardisedWorkflow() {
            @Override
            protected Task<Map<String, Object>>[] getTasksToExecute(CaseDetails caseDetails) {
                return new Task[0];
            }

            @Override
            protected Pair<String, Object>[] prepareContextVariables(CaseDetails caseDetails, String authToken) {
                throw exceptionToThrow;
            }
        };
        WorkflowException thrownException = assertThrows(WorkflowException.class, () -> classUnderTest.run(TEST_INCOMING_CASE_DETAILS, AUTH_TOKEN));
        assertThat(thrownException.getCause(), is(exceptionToThrow));
    }

    @Test
    public void shouldRethrowOriginalWorkflowException_IfOneIsThrown() {
        TaskException originatingTaskException = new TaskException("This will already be wrapped into a WorkflowException");
        when(firstTask.execute(any(), any())).thenThrow(originatingTaskException);

        StandardisedWorkflow classUnderTest = new StandardisedWorkflow() {
            @Override
            protected Task<Map<String, Object>>[] getTasksToExecute(CaseDetails caseDetails) {
                return new Task[] {
                    firstTask
                };
            }

            @Override
            protected Pair<String, Object>[] prepareContextVariables(CaseDetails caseDetails, String authToken) {
                return new Pair[] {
                    ImmutablePair.of("name1", "value1")
                };
            }
        };

        WorkflowException thrownException = assertThrows(WorkflowException.class, () -> classUnderTest.run(TEST_INCOMING_CASE_DETAILS, AUTH_TOKEN));
        assertThat(thrownException.getCause(), is(originatingTaskException));
    }

}