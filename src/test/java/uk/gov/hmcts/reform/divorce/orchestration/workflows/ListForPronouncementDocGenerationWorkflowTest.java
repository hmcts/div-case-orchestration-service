package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetFormattedDnCourtDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SyncBulkCaseListTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDivorceCaseRemovePronouncementDetailsWithinBulkTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class ListForPronouncementDocGenerationWorkflowTest {

    private static final String PRONOUNCEMENT_JUDGE_CCD_FIELD = "PronouncementJudge";
    private static final String LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID = "FL-DIV-GNO-ENG-00059.docx";


    @InjectMocks
    private ListForPronouncementDocGenerationWorkflow classToTest;

    @Mock
    private SetFormattedDnCourtDetails setFormattedDnCourtDetails;

    @Mock
    private DocumentGenerationTask documentGenerationTask;

    @Mock
    private AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Mock
    private SyncBulkCaseListTask syncBulkCaseListTask;

    @Mock
    private UpdateDivorceCaseRemovePronouncementDetailsWithinBulkTask removePronouncementDetailsTask;

    @Test
    public void callsTheRequiredTasksInOrder() throws TaskException, WorkflowException {
        final Map<String, Object> payload = new HashMap<>();
        payload.put(PRONOUNCEMENT_JUDGE_CCD_FIELD, "Mr Judge");

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();
        final TaskContext context = getTaskContext(caseDetails);

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(syncBulkCaseListTask.execute(context, payload)).thenReturn(payload);
        when(setFormattedDnCourtDetails.execute(context, payload)).thenReturn(payload);
        when(documentGenerationTask.execute(context, payload)).thenReturn(payload);
        when(addNewDocumentsToCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(removePronouncementDetailsTask.execute(context, payload)).thenReturn(payload);

        final Map<String, Object> result = classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        assertThat(result, is(payload));

        final InOrder inOrder = inOrder(
                syncBulkCaseListTask,
                setFormattedDnCourtDetails,
                documentGenerationTask,
            addNewDocumentsToCaseDataTask,
                removePronouncementDetailsTask
        );

        inOrder.verify(syncBulkCaseListTask).execute(context, payload);
        inOrder.verify(setFormattedDnCourtDetails).execute(context, payload);
        inOrder.verify(documentGenerationTask).execute(context, payload);
        inOrder.verify(addNewDocumentsToCaseDataTask).execute(context, payload);
        inOrder.verify(removePronouncementDetailsTask).execute(context, payload);
    }

    @Test
    public void givenCaseWithoutJudge_notCallDocumentGenerator() throws TaskException, WorkflowException {
        final Map<String, Object> payload = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        final TaskContext context = getTaskContext(caseDetails);
        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(syncBulkCaseListTask.execute(context, payload)).thenReturn(payload);
        when(removePronouncementDetailsTask.execute(context, payload)).thenReturn(payload);

        final Map<String, Object> result = classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        assertThat(result, is(payload));

        verify(syncBulkCaseListTask, times(1)).execute(context, payload);
        verify(setFormattedDnCourtDetails, never()).execute(context, payload);
        verify(documentGenerationTask, never()).execute(context, payload);
        verify(addNewDocumentsToCaseDataTask, never()).execute(context, payload);
        verify(removePronouncementDetailsTask, times(1)).execute(context, payload);
    }

    private TaskContext getTaskContext(final CaseDetails caseDetails) {
        final TaskContext context = new DefaultTaskContext();

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(DOCUMENT_TEMPLATE_ID, LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID);
        context.setTransientObject(DOCUMENT_TYPE, "caseListForPronouncement");
        context.setTransientObject(DOCUMENT_FILENAME, "caseListForPronouncement");
        return context;
    }
}
