package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.AddDaGrantedCertificateToDocumentsToPrintTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DaGrantedLetterGenerationTask;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SendDaGrantedNotificationWorkflowTest {

    @Mock
    private SendDaGrantedNotificationEmailTask sendDaGrantedNotificationEmailTask;

    @Mock
    private DaGrantedLetterGenerationTask daGrantedLetterGenerationTask;

    @Mock
    private AddDaGrantedCertificateToDocumentsToPrintTask addDaGrantedCertificateToDocumentsToPrintTask;

    @Mock
    private BulkPrinterTask bulkPrinterTask;

    @InjectMocks
    private SendDaGrantedNotificationWorkflow sendDaGrantedNotificationWorkflow;

    @Test
    public void runShouldCallSendDaGrantedNotificationEmailTaskWhenDigitalCommunication() throws Exception {
        Map<String, Object> casePayload = buildCaseData(YES_VALUE);

        when(sendDaGrantedNotificationEmailTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        Map<String, Object> result = sendDaGrantedNotificationWorkflow.run(buildCaseDetails(casePayload), AUTH_TOKEN);

        assertEquals(result, casePayload);

        verify(sendDaGrantedNotificationEmailTask, times(1)).execute(any(TaskContext.class), eq(casePayload));

        verify(daGrantedLetterGenerationTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(addDaGrantedCertificateToDocumentsToPrintTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(bulkPrinterTask, never()).execute(any(TaskContext.class), eq(casePayload));
    }

    @Test
    public void runShouldCallBulkPrintingWhenNoDigitalCommunication() throws Exception {
        Map<String, Object> casePayload = buildCaseData(NO_VALUE);

        when(daGrantedLetterGenerationTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(addDaGrantedCertificateToDocumentsToPrintTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(bulkPrinterTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        Map<String, Object> result = sendDaGrantedNotificationWorkflow.run(buildCaseDetails(casePayload), AUTH_TOKEN);

        assertEquals(result, casePayload);

        InOrder inOrder = inOrder(
            daGrantedLetterGenerationTask,
            addDaGrantedCertificateToDocumentsToPrintTask,
            bulkPrinterTask
        );

        inOrder.verify(daGrantedLetterGenerationTask).execute(any(TaskContext.class), eq(casePayload));
        inOrder.verify(addDaGrantedCertificateToDocumentsToPrintTask).execute(any(TaskContext.class), eq(casePayload));
        inOrder.verify(bulkPrinterTask).execute(any(TaskContext.class), eq(casePayload));

        verify(sendDaGrantedNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
    }

    private HashMap<String, Object> buildCaseData(String value) {
        return new HashMap<>(ImmutableMap.of(RESP_IS_USING_DIGITAL_CHANNEL, value));
    }

    private CaseDetails buildCaseDetails(Map<String, Object> casePayload) {
        return CaseDetails.builder()
            .caseId(CASE_TYPE_ID)
            .caseData(casePayload)
            .build();
    }
}
