package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithIdTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidatedCaseLinkTask;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_BULK_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class RemoveBulkCaseLinkWorkflowTest {

    @Mock
    private UpdateCaseInCCD updateCaseInCCD;

    @Mock
    private GetCaseWithIdTask getCaseWithId;

    @Mock
    private ValidatedCaseLinkTask validateBulkCaseLinkTask;

    @InjectMocks
    private RemoveBulkCaseLinkWorkflow classToTest;

    @Test
    public void givenWorkflow_thenExecuteTaskInOrder() throws WorkflowException, TaskException {
        when(getCaseWithId.execute(any(TaskContext.class), eq(DUMMY_CASE_DATA))).thenReturn(DUMMY_CASE_DATA);
        when(validateBulkCaseLinkTask.execute(any(TaskContext.class), eq(DUMMY_CASE_DATA))).thenReturn(DUMMY_CASE_DATA);
        when(updateCaseInCCD.execute(any(TaskContext.class), eq(DUMMY_CASE_DATA))).thenReturn(DUMMY_CASE_DATA);

        assertThat(classToTest.run(DUMMY_CASE_DATA, TEST_CASE_ID, TEST_BULK_CASE_ID, AUTH_TOKEN), is(DUMMY_CASE_DATA));

        final InOrder inOrder = inOrder(
            getCaseWithId,
            validateBulkCaseLinkTask,
            updateCaseInCCD
        );

        inOrder.verify(getCaseWithId).execute(any(TaskContext.class), eq(DUMMY_CASE_DATA));
        inOrder.verify(validateBulkCaseLinkTask).execute(any(TaskContext.class), eq(DUMMY_CASE_DATA));
        inOrder.verify(updateCaseInCCD).execute(any(TaskContext.class), eq(DUMMY_CASE_DATA));
    }


}
