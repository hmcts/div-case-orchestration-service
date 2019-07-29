package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.BulkWorkflowExecutionResult;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseAcceptedCasesEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdateCourtHearingEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdatePronouncementDateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.exception.BulkUpdateException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveBulkCaseLinkWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateCourtHearingDetailsWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdatePronouncementDateWorkflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_BULK_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_ACCEPTED_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CREATE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.LISTED_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.PRONOUNCED_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.REMOVED_CASE_LIST;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@RunWith(MockitoJUnitRunner.class)
public class BulkCaseServiceImplTest {

    private static final String FAILED_CASE_ID = "failedCaseId";
    private static final String TEST_CASE_ID_ONE = "caseId1";
    private static final String TEST_CASE_ID_TWO = "caseId2";

    @Mock
    private LinkBulkCaseWorkflow linkBulkCaseWorkflow;

    @Mock
    private UpdateCourtHearingDetailsWorkflow updateCourtHearingDetailsWorkflow;

    @Mock
    private UpdatePronouncementDateWorkflow updatePronouncementDateWorkflow;

    @Mock
    private UpdateBulkCaseWorkflow updateBulkCaseWorkflow;

    @Mock
    private RemoveBulkCaseLinkWorkflow removeBulkCaseLinkWorkflow;

    @InjectMocks
    private BulkCaseServiceImpl classToTest;

    @Test
    public void givenCaseList_thenExecuteCreateWorkflowForEachCase() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> caseData = ImmutableMap.of("SomeKey", "SomeValue");
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
            CCD_CASE_DATA_FIELD, ImmutableMap.of(CASE_LIST_KEY, Arrays.asList(caseData, caseData)));

        BulkWorkflowExecutionResult result = BulkWorkflowExecutionResult.builder().successStatus(true).build();
        when(linkBulkCaseWorkflow.executeWithRetriesForCreate(caseDetail, TEST_CASE_ID, AUTH_TOKEN)).thenReturn(result);
        BulkCaseCreateEvent event = new BulkCaseCreateEvent(taskContext, caseDetail);

        classToTest.handleBulkCaseCreateEvent(event);

        verify(linkBulkCaseWorkflow, times(1)).executeWithRetriesForCreate(caseDetail, TEST_CASE_ID, AUTH_TOKEN);
        verify(updateBulkCaseWorkflow, times(1)).run(Collections.emptyMap(), AUTH_TOKEN, TEST_CASE_ID, CREATE_EVENT);
    }

    @Test
    public void givenFailuresClasses_whenHandleBulkCase_thenBulkCaseIsNotUpdated() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> caseData = ImmutableMap.of("SomeKey", "SomeValue");
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
            CCD_CASE_DATA_FIELD, ImmutableMap.of(CASE_LIST_KEY, Arrays.asList(caseData, caseData)));

        BulkWorkflowExecutionResult result = BulkWorkflowExecutionResult.builder().successStatus(false).build();
        when(linkBulkCaseWorkflow.executeWithRetriesForCreate(caseDetail, TEST_CASE_ID, AUTH_TOKEN)).thenReturn(result);
        BulkCaseCreateEvent event = new BulkCaseCreateEvent(taskContext, caseDetail);

        try {
            classToTest.handleBulkCaseCreateEvent(event);
            Assert.fail("Expected bulkUpdateException");
        } catch (BulkUpdateException e) {
            assertThat(e.getMessage(),
                containsString(String.format("Failed to updating bulk case link for some cases on bulk case id %s", TEST_CASE_ID)));

        }
        verify(linkBulkCaseWorkflow, times(1)).executeWithRetriesForCreate(caseDetail, TEST_CASE_ID, AUTH_TOKEN);
        verify(updateBulkCaseWorkflow, never()).run(Collections.emptyMap(), AUTH_TOKEN, TEST_CASE_ID, CREATE_EVENT);
    }

    @Test
    public void givenRemovableCases_whenHandleBulkCase_thenBulkCaseIsUpdated() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> caseReferenceOne = Collections.singletonMap(CASE_REFERENCE_FIELD, TEST_CASE_ID_ONE);
        Map<String, Object> caseReferenceTwo = Collections.singletonMap(CASE_REFERENCE_FIELD, TEST_CASE_ID_TWO);
        Map<String, Object> caseDataOne = ImmutableMap.of(CASE_REFERENCE_FIELD, caseReferenceOne);
        Map<String, Object> caseDataTwo = ImmutableMap.of(CASE_REFERENCE_FIELD, caseReferenceTwo);
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
                CCD_CASE_DATA_FIELD, ImmutableMap.of(
                        CASE_LIST_KEY, Arrays.asList(
                                Collections.singletonMap(VALUE_KEY, caseDataOne),
                                Collections.singletonMap(VALUE_KEY, caseDataTwo)
                        ),
                        BULK_CASE_ACCEPTED_LIST_KEY, Arrays.asList(
                                Collections.singletonMap(VALUE_KEY, caseReferenceOne),
                                Collections.singletonMap(VALUE_KEY, caseReferenceTwo)
                        )
                ));

        BulkWorkflowExecutionResult result = BulkWorkflowExecutionResult.builder()
                .successStatus(true)
                .failedCases(Arrays.asList(caseDataOne))
                .removableCaseIds(ImmutableSet.of(TEST_CASE_ID_ONE))
                .build();
        when(linkBulkCaseWorkflow.executeWithRetriesForCreate(caseDetail, TEST_CASE_ID, AUTH_TOKEN)).thenReturn(result);
        BulkCaseCreateEvent event = new BulkCaseCreateEvent(taskContext, caseDetail);

        classToTest.handleBulkCaseCreateEvent(event);

        Map<String, Object> expectedUpdatePayload = ImmutableMap.of(
                CASE_LIST_KEY, Arrays.asList(Collections.singletonMap(VALUE_KEY, caseDataTwo)),
                BULK_CASE_ACCEPTED_LIST_KEY, Arrays.asList(Collections.singletonMap(VALUE_KEY, caseReferenceTwo))
        );

        verify(linkBulkCaseWorkflow, times(1)).executeWithRetriesForCreate(caseDetail, TEST_CASE_ID, AUTH_TOKEN);
        verify(updateBulkCaseWorkflow, times(1)).run(expectedUpdatePayload, AUTH_TOKEN, TEST_CASE_ID, CREATE_EVENT);
    }

    @Test
    public void givenBothFailedAndRemovableCases_whenHandleBulkCase_thenBulkCaseIsNotUpdated() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> caseReferenceOne = Collections.singletonMap(CASE_REFERENCE_FIELD, TEST_CASE_ID_ONE);
        Map<String, Object> caseReferenceTwo = Collections.singletonMap(CASE_REFERENCE_FIELD, TEST_CASE_ID_TWO);
        Map<String, Object> caseDataOne = ImmutableMap.of(CASE_REFERENCE_FIELD, caseReferenceOne);
        Map<String, Object> caseDataTwo = ImmutableMap.of(CASE_REFERENCE_FIELD, caseReferenceTwo);
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
                CCD_CASE_DATA_FIELD, ImmutableMap.of(
                        CASE_LIST_KEY, Arrays.asList(
                                Collections.singletonMap(VALUE_KEY, caseDataOne),
                                Collections.singletonMap(VALUE_KEY, caseDataTwo)
                        ),
                        BULK_CASE_ACCEPTED_LIST_KEY, Arrays.asList(
                                Collections.singletonMap(VALUE_KEY, caseReferenceOne),
                                Collections.singletonMap(VALUE_KEY, caseReferenceTwo)
                        )
                ));

        BulkWorkflowExecutionResult result = BulkWorkflowExecutionResult.builder()
                .successStatus(false)
                .failedCases(Arrays.asList(caseDataOne, caseDataTwo))
                .removableCaseIds(ImmutableSet.of(TEST_CASE_ID_ONE))
                .build();
        when(linkBulkCaseWorkflow.executeWithRetriesForCreate(caseDetail, TEST_CASE_ID, AUTH_TOKEN)).thenReturn(result);
        BulkCaseCreateEvent event = new BulkCaseCreateEvent(taskContext, caseDetail);

        try {
            classToTest.handleBulkCaseCreateEvent(event);
            Assert.fail("Expected bulkUpdateException");
        } catch (BulkUpdateException e) {
            assertThat(e.getMessage(),
                    containsString(String.format("Failed to updating bulk case link for some cases on bulk case id %s", TEST_CASE_ID)));

        }
        verify(linkBulkCaseWorkflow, times(1)).executeWithRetriesForCreate(caseDetail, TEST_CASE_ID, AUTH_TOKEN);
        verify(updateBulkCaseWorkflow, never()).run(Collections.emptyMap(), AUTH_TOKEN, TEST_CASE_ID, CREATE_EVENT);
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

        when(updateCourtHearingDetailsWorkflow.executeWithRetries(caseDetail, TEST_CASE_ID, AUTH_TOKEN)).thenReturn(true);

        classToTest.handleBulkCaseUpdateCourtHearingEvent(event);

        verify(updateCourtHearingDetailsWorkflow, times(1))
            .executeWithRetries(caseDetail, TEST_CASE_ID, AUTH_TOKEN);
        verify(updateBulkCaseWorkflow, times(1)).run(Collections.emptyMap(), AUTH_TOKEN, TEST_CASE_ID, LISTED_EVENT);
    }

    @Test(expected = BulkUpdateException.class)
    public void givenError_whenHandleUpdateHearingCase_thenRaiseBulkUpdateException() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        Map<String, Object> caseData = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID));
        Map<String, Object> failedCaseData = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, FAILED_CASE_ID));
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
            CCD_CASE_DATA_FIELD, ImmutableMap.of(BULK_CASE_ACCEPTED_LIST_KEY, Arrays.asList(failedCaseData, caseData)));

        when(updateCourtHearingDetailsWorkflow
            .executeWithRetries(caseDetail, TEST_CASE_ID, AUTH_TOKEN))
            .thenReturn(false);

        BulkCaseUpdateCourtHearingEvent event = new BulkCaseUpdateCourtHearingEvent(taskContext, caseDetail);
        classToTest.handleBulkCaseUpdateCourtHearingEvent(event);
        verify(updateBulkCaseWorkflow, never()).run(any(), any(), any(), any());
    }

    @Test(expected = BulkUpdateException.class)
    public void givenEmptyData_thenUpdatePronouncementDateIsNotRunAndErrorIsThrown() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();

        BulkCaseUpdatePronouncementDateEvent event = new BulkCaseUpdatePronouncementDateEvent(taskContext, Collections.emptyMap());
        classToTest.handleBulkCaseUpdatePronouncementDateEvent(event);

        verify(updatePronouncementDateWorkflow, never()).run(any(), any(), any());
        verify(updateBulkCaseWorkflow, never()).run(any(), any(), any(), any());
    }

    @Test
    public void givenCaseList_thenExecuteUpdatePronouncementDateWorkflowForEachCase() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        // Setup CaseLinks
        Map<String, Object> caseLink = ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID);
        Map<String, Object> caseData = ImmutableMap.of(VALUE_KEY, caseLink);
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
                CCD_CASE_DATA_FIELD, ImmutableMap.of(BULK_CASE_ACCEPTED_LIST_KEY, Arrays.asList(caseData, caseData)));

        BulkCaseUpdatePronouncementDateEvent event = new BulkCaseUpdatePronouncementDateEvent(taskContext, caseDetail);

        when(updatePronouncementDateWorkflow.executeWithRetries(caseDetail, TEST_CASE_ID, AUTH_TOKEN)).thenReturn(true);

        classToTest.handleBulkCaseUpdatePronouncementDateEvent(event);

        verify(updatePronouncementDateWorkflow, times(1))
                .executeWithRetries(caseDetail, TEST_CASE_ID, AUTH_TOKEN);
        verify(updateBulkCaseWorkflow, times(1)).run(Collections.emptyMap(), AUTH_TOKEN, TEST_CASE_ID, PRONOUNCED_EVENT);
    }

    @Test(expected = BulkUpdateException.class)
    public void givenException_whenHandleUpdatePronouncementDate_thenExecuteOtherCases() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        Map<String, Object> caseData = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, TEST_CASE_ID));
        Map<String, Object> failedCaseData = ImmutableMap.of(VALUE_KEY, ImmutableMap.of(CASE_REFERENCE_FIELD, FAILED_CASE_ID));
        Map<String, Object> caseDetail = ImmutableMap.of(ID, TEST_CASE_ID,
                CCD_CASE_DATA_FIELD, ImmutableMap.of(BULK_CASE_ACCEPTED_LIST_KEY, Arrays.asList(failedCaseData, caseData)));

        when(updatePronouncementDateWorkflow
                .executeWithRetries(caseDetail, TEST_CASE_ID, AUTH_TOKEN))
                .thenReturn(false);

        BulkCaseUpdatePronouncementDateEvent event = new BulkCaseUpdatePronouncementDateEvent(taskContext, caseDetail);
        classToTest.handleBulkCaseUpdatePronouncementDateEvent(event);
        verify(updateBulkCaseWorkflow, never()).run(any(), any(), any(), any());
    }

    @Test
    public void whenRemoveBulkCaseLink_thenProcessAllCases() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(REMOVED_CASE_LIST, Arrays.asList(TEST_CASE_ID, TEST_CASE_ID));

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_BULK_CASE_ID).caseData(new HashMap<>()).build();
        BulkCaseAcceptedCasesEvent event = new BulkCaseAcceptedCasesEvent(taskContext, caseDetails);

        classToTest.handleBulkCaseAcceptedCasesEvent(event);

        verify(removeBulkCaseLinkWorkflow, times(2)).run(caseDetails.getCaseData(), TEST_CASE_ID, TEST_BULK_CASE_ID,AUTH_TOKEN);
    }

    @Test
    public void givenError_whenRemoveBulkCaseLink_thenProcessOtherCases() throws WorkflowException {
        TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(REMOVED_CASE_LIST, Arrays.asList(TEST_CASE_ID, FAILED_CASE_ID, TEST_CASE_ID));

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_BULK_CASE_ID).caseData(new HashMap<>()).build();
        BulkCaseAcceptedCasesEvent event = new BulkCaseAcceptedCasesEvent(taskContext, caseDetails);
        Map<String, Object> expectedCaseData = new HashMap<>();
        when(removeBulkCaseLinkWorkflow
            .run(caseDetails.getCaseData(), TEST_CASE_ID, TEST_BULK_CASE_ID,AUTH_TOKEN))
            .thenReturn(expectedCaseData);
        when(removeBulkCaseLinkWorkflow
            .run(caseDetails.getCaseData(), FAILED_CASE_ID, TEST_BULK_CASE_ID,AUTH_TOKEN))
            .thenThrow(new WorkflowException("Test error"));
        classToTest.handleBulkCaseAcceptedCasesEvent(event);

        verify(removeBulkCaseLinkWorkflow, times(2)).run(caseDetails.getCaseData(), TEST_CASE_ID, TEST_BULK_CASE_ID,AUTH_TOKEN);
        verify(removeBulkCaseLinkWorkflow, times(1)).run(caseDetails.getCaseData(), FAILED_CASE_ID, TEST_BULK_CASE_ID,AUTH_TOKEN);
    }
}
