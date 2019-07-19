package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SyncBulkCaseListTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDivorceCasesWithinBulkTask;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class BulkCaseRemoveCasesWorkflowUTest {

    @Mock
    private SyncBulkCaseListTask syncBulkCaseListTask;

    @Mock
    private UpdateDivorceCasesWithinBulkTask updateDivorceCasesWithinBulkTask;

    @InjectMocks
    private BulkCaseRemoveCasesWorkflow bulkCaseRemoveCasesWorkflow;

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(DUMMY_CASE_DATA).build();
        Map<String, Object> testData = DUMMY_CASE_DATA;
        when(syncBulkCaseListTask.execute(any(TaskContext.class), eq(testData))).thenReturn(testData);
        when(updateDivorceCasesWithinBulkTask.execute(any(TaskContext.class), eq(testData))).thenReturn(testData);

        assertEquals(testData, bulkCaseRemoveCasesWorkflow.run(caseDetails, AUTH_TOKEN));

        final InOrder inOrder = inOrder(
            syncBulkCaseListTask,
            updateDivorceCasesWithinBulkTask
        );

        inOrder.verify(syncBulkCaseListTask).execute(any(TaskContext.class), eq(testData));
        inOrder.verify(updateDivorceCasesWithinBulkTask).execute(any(TaskContext.class), eq(testData));
    }
}
