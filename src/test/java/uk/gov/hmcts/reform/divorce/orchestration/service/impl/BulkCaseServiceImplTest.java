package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdateCourtHearingEvent;
import uk.gov.hmcts.reform.divorce.orchestration.exception.BulkUpdateException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateCourtHearingDetailsWorkflow;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_ACCEPTED_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.LISTED_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@RunWith(MockitoJUnitRunner.class)
public class BulkCaseServiceImplTest {

    private static final String FAILED_CASE_ID = "failedCaseId";

    @Mock
    private LinkBulkCaseWorkflow linkBulkCaseWorkflow;

    @Mock
    private UpdateCourtHearingDetailsWorkflow updateCourtHearingDetailsWorkflow;

    @Mock
    private UpdateBulkCaseWorkflow updateBulkCaseWorkflow;

    @InjectMocks
    private BulkCaseServiceImpl classToTest;

    @Test
    public void givenEmptyData_thenCreateWorkflowIsNotExecuted() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();

        BulkCaseCreateEvent event = new BulkCaseCreateEvent(taskContext, Collections.emptyMap());
        classToTest.handleBulkCaseCreateEvent(event);

        verify(linkBulkCaseWorkflow, never()).run(any(), any(), any());
    }

    @Test
    public void givenCaseList_thenExecuteCreateWorkflowForEachCase() throws WorkflowException {
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
    public void givenException_whenHandleCreateCase_thenExecuteOtherCases() throws WorkflowException {
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

    @Test(expected = BulkUpdateException.class)
    public void givenEmptyData_thenUpdateHearingWorkflowIsNotExecutedAndErrorIsThrown() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();

        BulkCaseUpdateCourtHearingEvent event = new BulkCaseUpdateCourtHearingEvent(taskContext, Collections.emptyMap());
        classToTest.handleBulkCaseUpdateCourtHearingEvent(event);

        verify(updateCourtHearingDetailsWorkflow, never()).run(any(), any(), any());
        verify(updateBulkCaseWorkflow, never()).run(any(), any(), any(), any());
    }

    @Test
    public void givenCaseList_thenExecuteUpdateHearingWorkflowForEachCase() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        // Setup CaseLinks
        Map<String, Object> caseLink = ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID);
        Map<String, Object> caseData = ImmutableMap.of(VALUE_KEY, caseLink);
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
                CCD_CASE_DATA_FIELD, ImmutableMap.of(BULK_CASE_ACCEPTED_LIST_KEY, Arrays.asList(caseData, caseData)));

        BulkCaseUpdateCourtHearingEvent event = new BulkCaseUpdateCourtHearingEvent(taskContext, caseDetail);
        classToTest.handleBulkCaseUpdateCourtHearingEvent(event);
        verify(updateCourtHearingDetailsWorkflow, times(2))
                .run((Map<String, Object>) caseDetail.get(CCD_CASE_DATA_FIELD), TEST_CASE_ID, AUTH_TOKEN);
        verify(updateBulkCaseWorkflow, times(1)).run(Collections.emptyMap(), AUTH_TOKEN, TEST_CASE_ID, LISTED_EVENT);
    }

    @Test(expected = BulkUpdateException.class)
    public void givenException_whenHandleUpdateHearingCase_thenExecuteOtherCases() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        Map<String, Object> caseData = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID));
        Map<String, Object> failedCaseData = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, FAILED_CASE_ID));
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
                CCD_CASE_DATA_FIELD, ImmutableMap.of(BULK_CASE_ACCEPTED_LIST_KEY, Arrays.asList(failedCaseData, caseData)));

        when(updateCourtHearingDetailsWorkflow
                .run((Map<String, Object>) caseDetail.get(CCD_CASE_DATA_FIELD), FAILED_CASE_ID, AUTH_TOKEN))
                .thenThrow(new WorkflowException("Workflow failed"));

        BulkCaseUpdateCourtHearingEvent event = new BulkCaseUpdateCourtHearingEvent(taskContext, caseDetail);
        classToTest.handleBulkCaseUpdateCourtHearingEvent(event);
        verify(updateCourtHearingDetailsWorkflow, times(1))
                .run((Map<String, Object>) caseDetail.get(CCD_CASE_DATA_FIELD), FAILED_CASE_ID, AUTH_TOKEN);
        verify(updateCourtHearingDetailsWorkflow, times(1))
                .run((Map<String, Object>) caseDetail.get(CCD_CASE_DATA_FIELD), TEST_CASE_ID, AUTH_TOKEN);
        verify(updateBulkCaseWorkflow, never()).run(any(), any(), any(), any());
    }
}
