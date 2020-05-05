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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedDocumentsToBulkPrintTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedNotificationEmailTask;

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
    private DocumentGenerationTask documentGenerationTask;

    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Mock
    private SendDaGrantedDocumentsToBulkPrintTask sendDaGrantedDocumentsToBulkPrint;

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
        verify(documentGenerationTask, never()).execute(any(TaskContext.class), eq(casePayload));
    }

    @Test
    public void notifyApplicantCanFinaliseDivorceTaskIsExecuted_send_email_if_is_not_digital_channel() throws Exception {
        Map<String, Object> casePayload = new HashMap<>(ImmutableMap.of(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE));
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(casePayload)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(documentGenerationTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(caseFormatterAddDocuments.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(sendDaGrantedDocumentsToBulkPrint.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        Map<String, Object> result = sendDaGrantedNotificationWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN_JSON_KEY);

        assertEquals(casePayload, result);

        verify(sendDaGrantedNotificationEmail,never()).execute(any(TaskContext.class), eq(casePayload));

        verify(documentGenerationTask, times(1)).execute(any(TaskContext.class), eq(casePayload));
        verify(caseFormatterAddDocuments, times(1)).execute(any(TaskContext.class), eq(casePayload));
        verify(sendDaGrantedDocumentsToBulkPrint, times(1)).execute(any(TaskContext.class), eq(casePayload));
    }

}