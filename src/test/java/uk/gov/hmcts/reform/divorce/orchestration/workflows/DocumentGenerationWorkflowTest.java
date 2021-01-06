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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetFormattedDnCourtDetails;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGenerationWorkflowTest {

    @Mock
    private SetFormattedDnCourtDetails setFormattedDnCourtDetails;

    @Mock
    private DocumentGenerationTask documentGenerationTask;

    @Mock
    private AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @InjectMocks
    private DocumentGenerationWorkflow documentGenerationWorkflow;

    private static final String COE_WELSH_TEMPLATE_ID = "FL-DIV-GNO-WEL-00238.docx";

    @Test
    public void callsTheRequiredTasksInOrder() throws WorkflowException {
        final TaskContext context = new DefaultTaskContext();
        final Map<String, Object> payload = new HashMap<>();

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(DOCUMENT_TEMPLATE_ID, "a");
        context.setTransientObject(DOCUMENT_TYPE, "b");
        context.setTransientObject(DOCUMENT_FILENAME, "c");

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(setFormattedDnCourtDetails.execute(context, payload)).thenReturn(payload);
        when(documentGenerationTask.execute(context, payload)).thenReturn(payload);
        when(addNewDocumentsToCaseDataTask.execute(context, payload)).thenReturn(payload);

        final Map<String, Object> result = documentGenerationWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN, "a", "b", "c");

        assertThat(result, is(payload));

        final InOrder inOrder = inOrder(setFormattedDnCourtDetails, documentGenerationTask, addNewDocumentsToCaseDataTask);

        inOrder.verify(setFormattedDnCourtDetails).execute(context, payload);
        inOrder.verify(documentGenerationTask).execute(context, payload);
        inOrder.verify(addNewDocumentsToCaseDataTask).execute(context, payload);
    }

    @Test
    public void testToPickTemplateIdFromConfiguration() throws WorkflowException {
        final TaskContext context = new DefaultTaskContext();
        final Map<String, Object> payload = new HashMap<>();
        payload.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(DOCUMENT_TEMPLATE_ID, COE_WELSH_TEMPLATE_ID);
        context.setTransientObject(DOCUMENT_TYPE, "coe");
        context.setTransientObject(DOCUMENT_FILENAME, "c");

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(setFormattedDnCourtDetails.execute(context, payload)).thenReturn(payload);
        when(documentGenerationTask.execute(context, payload)).thenReturn(payload);
        when(addNewDocumentsToCaseDataTask.execute(context, payload)).thenReturn(payload);

        documentGenerationWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN, "a", "coe", "c");

        final InOrder inOrder = inOrder(setFormattedDnCourtDetails, documentGenerationTask, addNewDocumentsToCaseDataTask);
        inOrder.verify(setFormattedDnCourtDetails).execute(context, payload);
        inOrder.verify(documentGenerationTask).execute(context, payload);
        inOrder.verify(addNewDocumentsToCaseDataTask).execute(context, payload);
    }

    @Test
    public void testToContinueWithProvidedTemplateId_WhenInvalidTemplateLogicalNameIsProvided() throws WorkflowException {
        final TaskContext context = new DefaultTaskContext();
        final Map<String, Object> payload = new HashMap<>();
        payload.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(DOCUMENT_TEMPLATE_ID, "a");
        context.setTransientObject(DOCUMENT_TYPE, "invalidTemplateLogicalName");
        context.setTransientObject(DOCUMENT_FILENAME, "c");

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(setFormattedDnCourtDetails.execute(context, payload)).thenReturn(payload);
        when(documentGenerationTask.execute(context, payload)).thenReturn(payload);
        when(addNewDocumentsToCaseDataTask.execute(context, payload)).thenReturn(payload);

        documentGenerationWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN, "a", "invalidTemplateLogicalName", "c");

        final InOrder inOrder = inOrder(setFormattedDnCourtDetails, documentGenerationTask, addNewDocumentsToCaseDataTask);
        inOrder.verify(setFormattedDnCourtDetails).execute(context, payload);
        inOrder.verify(documentGenerationTask).execute(context, payload);
        inOrder.verify(addNewDocumentsToCaseDataTask).execute(context, payload);
    }

}