package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MultipleDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DA_GRANTED_OFFLINE_PACK_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_GENERATION_REQUESTS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.DOCUMENT_TYPES_TO_PRINT;

@RunWith(MockitoJUnitRunner.class)
public class SendDaGrantedNotificationWorkflowTest {

    @Mock
    private SendDaGrantedNotificationEmailTask sendDaGrantedNotificationEmail;

    @Mock
    private MultipleDocumentGenerationTask documentsGenerationTask;

    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Mock
    private FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

    @Mock
    private BulkPrinterTask bulkPrinterTask;

    @InjectMocks
    private SendDaGrantedNotificationWorkflow sendDaGrantedNotificationWorkflow;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @Test
    public void notifyApplicantCanFinaliseDivorceTaskIsExecuted_send_email_if_is_digital_channel() throws Exception {
        Map<String, Object> casePayload = buildTestCasePayload(YES_VALUE);
        final CaseDetails caseDetails = buildTestCaseDetails(casePayload);
        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(sendDaGrantedNotificationEmail.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        Map<String, Object> result = sendDaGrantedNotificationWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN_JSON_KEY);

        assertEquals(casePayload, result);

        verify(sendDaGrantedNotificationEmail, times(1)).execute(any(TaskContext.class), eq(casePayload));
        verify(documentsGenerationTask, never()).execute(any(TaskContext.class), eq(casePayload));
    }

    @Test
    public void notifyApplicantCanFinaliseDivorceTaskIsExecuted_send_email_if_is_not_digital_channel() throws Exception {
        Map<String, Object> casePayload = buildTestCasePayload(NO_VALUE);
        final CaseDetails caseDetails = buildTestCaseDetails(casePayload);
        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(documentsGenerationTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(caseFormatterAddDocuments.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(fetchPrintDocsFromDmStore.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(bulkPrinterTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        Map<String, Object> result = sendDaGrantedNotificationWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN_JSON_KEY);

        assertEquals(casePayload, result);

        verify(sendDaGrantedNotificationEmail, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(documentsGenerationTask, times(1)).execute(any(TaskContext.class), eq(casePayload));
        verify(caseFormatterAddDocuments, times(1)).execute(any(TaskContext.class), eq(casePayload));
        verify(fetchPrintDocsFromDmStore, times(1)).execute(any(TaskContext.class), eq(casePayload));
        verify(bulkPrinterTask, times(1)).execute(any(TaskContext.class), eq(casePayload));
    }

    @Test
    public void testThatDocumentsGenerationTask_is_called_with_required_documents() throws TaskException, WorkflowException {
        Map<String, Object> casePayload = buildTestCasePayload(NO_VALUE);
        final CaseDetails caseDetails = buildTestCaseDetails(casePayload);
        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(documentsGenerationTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        sendDaGrantedNotificationWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN_JSON_KEY);

        verify(documentsGenerationTask).execute(taskContextArgumentCaptor.capture(), eq(caseDetails.getCaseData()));
        TaskContext taskContext = taskContextArgumentCaptor.getValue();
        List<DocumentGenerationRequest> documentGenerationRequestList = taskContext.getTransientObject(DOCUMENT_GENERATION_REQUESTS_KEY);

        assertThat(documentGenerationRequestList.size(), is(2));

        assertThat(documentGenerationRequestList.get(0).getDocumentTemplateId(), is(DECREE_ABSOLUTE_LETTER_TEMPLATE_ID));
        assertThat(documentGenerationRequestList.get(0).getDocumentFileName(), is(DECREE_ABSOLUTE_LETTER_FILENAME));
        assertThat(documentGenerationRequestList.get(0).getDocumentType(), is(DECREE_ABSOLUTE_LETTER_DOCUMENT_TYPE));

        assertThat(documentGenerationRequestList.get(1).getDocumentTemplateId(), is(DECREE_ABSOLUTE_TEMPLATE_ID));
        assertThat(documentGenerationRequestList.get(1).getDocumentFileName(), is(DECREE_ABSOLUTE_FILENAME));
        assertThat(documentGenerationRequestList.get(1).getDocumentType(), is(DECREE_ABSOLUTE_DOCUMENT_TYPE));

        List<String> documentTypesToPrint = documentGenerationRequestList.stream()
            .map(DocumentGenerationRequest::getDocumentType)
            .collect(Collectors.toList());

        verifyBulkPrintIsCalledWithCorrectData(documentTypesToPrint);
    }

    private void verifyBulkPrintIsCalledWithCorrectData(List<String> documentTypesToPrint) {
        Map<String, Object> casePayload = buildTestCasePayload(NO_VALUE);
        final CaseDetails caseDetails = buildTestCaseDetails(casePayload);

        TaskContext bulkPrintTaskContext = taskContextArgumentCaptor.getValue();
        assertThat(bulkPrintTaskContext.getTransientObject(CASE_DETAILS_JSON_KEY), is(caseDetails));
        assertThat(bulkPrintTaskContext.getTransientObject(BULK_PRINT_LETTER_TYPE), is(DA_GRANTED_OFFLINE_PACK_RESPONDENT));
        assertThat(bulkPrintTaskContext.getTransientObject(DOCUMENT_TYPES_TO_PRINT), equalTo(documentTypesToPrint));
    }

    private HashMap<String, Object> buildTestCasePayload(String value) {
        return new HashMap<>(ImmutableMap.of(RESP_IS_USING_DIGITAL_CHANNEL, value));
    }

    private CaseDetails buildTestCaseDetails(Map<String, Object> casePayload) {
        return CaseDetails.builder()
            .caseId(CASE_TYPE_ID)
            .state(CASE_STATE_JSON_KEY)
            .caseData(casePayload)
            .build();
    }
}
