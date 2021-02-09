package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetFormattedDnCourtDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PAYLOAD_TO_RETURN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COE;

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

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    private static final String COE_WELSH_TEMPLATE_ID = "FL-DIV-GNO-WEL-00238.docx";

    private static final String TEST_DEFAULT_TEMPLATE_ID = "a";
    private static final String TEST_CCD_DOCUMENT_TYPE = "b";
    private static final String TEST_FILE_NAME = "c";

    private Map<String, Object> incomingWelshPayload;
    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        incomingWelshPayload = new HashMap<>();
        incomingWelshPayload.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);

        caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(incomingWelshPayload)
            .build();

        when(setFormattedDnCourtDetails.execute(any(), eq(incomingWelshPayload))).thenReturn(TEST_PAYLOAD_TO_RETURN);
        when(documentGenerationTask.execute(any(), eq(TEST_PAYLOAD_TO_RETURN))).thenReturn(TEST_PAYLOAD_TO_RETURN);
        when(addNewDocumentsToCaseDataTask.execute(any(), eq(TEST_PAYLOAD_TO_RETURN))).thenReturn(TEST_PAYLOAD_TO_RETURN);
    }

    @Test
    public void shouldPickLanguageAppropriateTemplateIdFromDocumentTypeWithGivenTemplateLogicalName() throws WorkflowException {
        Map<String, Object> returnedCaseData = documentGenerationWorkflow.run(caseDetails, AUTH_TOKEN, TEST_CCD_DOCUMENT_TYPE,
            TEST_DEFAULT_TEMPLATE_ID, "coe", TEST_FILE_NAME);

        assertThat(returnedCaseData, equalTo(TEST_PAYLOAD_TO_RETURN));
        assertTasksAreCalledInOrderWithContextMatchingExpectedParameters(COE_WELSH_TEMPLATE_ID);
    }

    @Test
    public void shouldUseDefaultTemplateId_WhenGivenTemplateLogicalNameIsNotRegistered() throws WorkflowException {
        Map<String, Object> returnedCaseData = documentGenerationWorkflow.run(caseDetails, AUTH_TOKEN, TEST_CCD_DOCUMENT_TYPE,
            TEST_DEFAULT_TEMPLATE_ID, "invalidTemplateLogicalName", TEST_FILE_NAME);

        assertThat(returnedCaseData, equalTo(TEST_PAYLOAD_TO_RETURN));
        assertTasksAreCalledInOrderWithContextMatchingExpectedParameters(TEST_DEFAULT_TEMPLATE_ID);
    }

    @Test
    public void shouldPickLanguageAppropriateTemplateIdFromGivenDocumentType() throws WorkflowException {
        Map<String, Object> returnedCaseData = documentGenerationWorkflow.run(caseDetails, AUTH_TOKEN, TEST_CCD_DOCUMENT_TYPE, COE, TEST_FILE_NAME);

        assertThat(returnedCaseData, equalTo(TEST_PAYLOAD_TO_RETURN));
        assertTasksAreCalledInOrderWithContextMatchingExpectedParameters(COE_WELSH_TEMPLATE_ID);
    }

    private void assertTasksAreCalledInOrderWithContextMatchingExpectedParameters(String expectedTemplateId) {
        final InOrder inOrder = inOrder(setFormattedDnCourtDetails, documentGenerationTask, addNewDocumentsToCaseDataTask);
        inOrder.verify(setFormattedDnCourtDetails).execute(taskContextArgumentCaptor.capture(), eq(incomingWelshPayload));
        inOrder.verify(documentGenerationTask).execute(taskContextArgumentCaptor.capture(), eq(TEST_PAYLOAD_TO_RETURN));
        inOrder.verify(addNewDocumentsToCaseDataTask).execute(taskContextArgumentCaptor.capture(), eq(TEST_PAYLOAD_TO_RETURN));

        List<TaskContext> capturedTaskContexts = taskContextArgumentCaptor.getAllValues();
        assertThat(capturedTaskContexts, hasSize(3));
        capturedTaskContexts.forEach(context -> {
            assertThat(context.getTransientObject(CASE_DETAILS_JSON_KEY), equalTo(caseDetails));
            assertThat(context.getTransientObject(AUTH_TOKEN_JSON_KEY), equalTo(AUTH_TOKEN));
            assertThat(context.getTransientObject(DOCUMENT_TEMPLATE_ID), equalTo(expectedTemplateId));
            assertThat(context.getTransientObject(DOCUMENT_TYPE), equalTo(TEST_CCD_DOCUMENT_TYPE));
            assertThat(context.getTransientObject(DOCUMENT_FILENAME), equalTo(TEST_FILE_NAME));
        });
    }

}