package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mockito.InOrder;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Verificators {

    public static void verifyTaskWasCalled(Task<Map<String, Object>> task, Map<String, Object> caseData) throws TaskException {
        verify(task).execute(any(TaskContext.class), eq(caseData));
    }

    public static void verifyTaskWasNeverCalled(Task<Map<String, Object>> task) throws TaskException {
        verify(task, never()).execute(any(TaskContext.class), anyMap());
    }

    public static void verifyTasksCalledInOrder(Map<String, Object> caseData, Object... tasks) throws TaskException {
        InOrder inOrder = inOrder(tasks);

        for (Object task : tasks) {
            inOrder.verify((Task<Map<String, Object>> )task).execute(any(TaskContext.class), eq(caseData));
        }
    }

    @SafeVarargs
    public static void mockTasksExecution(Map<String, Object> caseData, Task<Map<String, Object>>... tasksToMock) throws TaskException {
        for (Task<Map<String, Object>> task : tasksToMock) {
            when(task.execute(any(TaskContext.class), eq(caseData))).thenReturn(caseData);
        }
    }
}
