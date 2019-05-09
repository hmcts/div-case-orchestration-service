package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkBulkCaseWorkflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@RunWith(MockitoJUnitRunner.class)
public class BulkCaseServiceImplTest {

    @Mock
    private LinkBulkCaseWorkflow linkBulkCaseWorkflow;

    @InjectMocks
    private BulkCaseServiceImpl classToTest;

    @Test
    public void givenEmptyData_thenWorkflowIsNotExecuted() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();

        BulkCaseCreateEvent event = new BulkCaseCreateEvent(taskContext, Collections.emptyMap());
        classToTest.handleBulkCaseCreateEvent(event);

        verify(linkBulkCaseWorkflow, never()).run(any(), any(), any());
    }

    @Test
    public void givenCaseList_thenExecuteWorkflowForEachCase() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> caseData = ImmutableMap.of("SomeKey", "SomeValue");
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
            CCD_CASE_DATA_FIELD, ImmutableMap.of(CASE_LIST_KEY, Arrays.asList(caseData, caseData)));

        BulkCaseCreateEvent event = new BulkCaseCreateEvent(taskContext, caseDetail);
        classToTest.handleBulkCaseCreateEvent(event);
        verify(linkBulkCaseWorkflow, times(2)).run(caseData, TEST_CASE_ID, AUTH_TOKEN);
    }

    @Test
    public void givenException_whenHandleCase_thenExecuteOtherCases() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> failedCaseData = ImmutableMap.of("ErrorKey", "SomeValue");
        Map<String, Object> correctCaseData = ImmutableMap.of("SomeKey", "SomeValue");
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
            CCD_CASE_DATA_FIELD, ImmutableMap.of(CASE_LIST_KEY, Arrays.asList(failedCaseData, correctCaseData)));


        when(linkBulkCaseWorkflow.run(failedCaseData, TEST_CASE_ID, AUTH_TOKEN)).thenThrow(new WorkflowException("Workflow failed"));

        BulkCaseCreateEvent event = new BulkCaseCreateEvent(taskContext, caseDetail);
        classToTest.handleBulkCaseCreateEvent(event);
        verify(linkBulkCaseWorkflow, times(1)).run(failedCaseData, TEST_CASE_ID, AUTH_TOKEN);
        verify(linkBulkCaseWorkflow, times(1)).run(correctCaseData, TEST_CASE_ID, AUTH_TOKEN);

    }
}
