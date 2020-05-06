package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MultipleDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedDocumentsToBulkPrintTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SendDaGrantedNotificationWorkflowTest {

    @Mock
    private SendDaGrantedNotificationEmailTask sendDaGrantedNotificationEmail;

    @Mock
    private  MultipleDocumentGenerationTask documentsGenerationTask;

    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Mock
    private FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

    @Mock
    private BulkPrinter bulkPrinterTask;

    @InjectMocks
    private SendDaGrantedNotificationWorkflow sendDaGrantedNotificationWorkflow;

    @Test
    public void notifyApplicantCanFinaliseDivorceTaskIsExecuted_send_email_if_is_digital_channel() throws Exception {
        Map<String, Object> casePayload = new HashMap<>(ImmutableMap.of(RESP_IS_USING_DIGITAL_CHANNEL, YES_VALUE));
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(casePayload)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(sendDaGrantedNotificationEmail.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        Map<String, Object> result = sendDaGrantedNotificationWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN_JSON_KEY);

        assertEquals(casePayload, result);

        verify(sendDaGrantedNotificationEmail,times(1)).execute(any(TaskContext.class), eq(casePayload));
        verify(documentsGenerationTask, never()).execute(any(TaskContext.class), eq(casePayload));
    }

    @Test
    public void notifyApplicantCanFinaliseDivorceTaskIsExecuted_send_email_if_is_not_digital_channel() throws Exception {
        Map<String, Object> casePayload = new HashMap<>(ImmutableMap.of(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE));
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(casePayload)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(documentsGenerationTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(caseFormatterAddDocuments.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(fetchPrintDocsFromDmStore.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(bulkPrinterTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        Map<String, Object> result = sendDaGrantedNotificationWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN_JSON_KEY);

        assertEquals(casePayload, result);

        verify(sendDaGrantedNotificationEmail,never()).execute(any(TaskContext.class), eq(casePayload));

        verify(documentsGenerationTask, times(1)).execute(any(TaskContext.class), eq(casePayload));
        verify(caseFormatterAddDocuments, times(1)).execute(any(TaskContext.class), eq(casePayload));
        verify(fetchPrintDocsFromDmStore, times(1)).execute(any(TaskContext.class), eq(casePayload));
        verify(bulkPrinterTask, times(1)).execute(any(TaskContext.class), eq(casePayload));
    }

}