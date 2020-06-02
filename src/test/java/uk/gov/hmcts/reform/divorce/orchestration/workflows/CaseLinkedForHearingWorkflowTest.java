package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCertificateOfEntitlementNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentCertificateOfEntitlementNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.AddDocumentToDocumentsToPrintTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CertificateOfEntitlementLetterGenerationTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.*;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.CaseLinkedForHearingWorkflow.CASE_ID_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CaseLinkedForHearingWorkflowTest {

    @Mock
    private SendPetitionerCertificateOfEntitlementNotificationEmailTask sendPetitionerCertificateOfEntitlementNotificationEmailTask;

    @Mock
    private SendRespondentCertificateOfEntitlementNotificationEmailTask sendRespondentCertificateOfEntitlementNotificationEmailTask;

    @Mock
    private SendCoRespondentGenericUpdateNotificationEmailTask sendCoRespondentGenericUpdateNotificationEmailTask;

    @Mock
    private CertificateOfEntitlementLetterGenerationTask certificateOfEntitlementLetterGenerationTask;

    @Mock
    private AddDocumentToDocumentsToPrintTask addDocumentToDocumentsToPrintTask;

    @Mock
    private BulkPrinterTask bulkPrinterTask;

    @InjectMocks
    private CaseLinkedForHearingWorkflow caseLinkedForHearingWorkflow;

    @Captor
    private ArgumentCaptor<TaskContext> contextCaptor;

    @Mock
    private FeatureToggleService featureToggleService;


    @Test
    public void runShouldCallSendCertificateOfEntitlementNotificationEmailTasksWhenDigitalCommunication() throws TaskException, WorkflowException {
        Map<String, Object> casePayload = buildCaseData(YES_VALUE);
        setupTasksToReturn(casePayload);

        Map<String, Object> returnedPayload = caseLinkedForHearingWorkflow.run(buildCaseDetails(casePayload), AUTH_TOKEN);

        assertThat(returnedPayload, is(equalTo(casePayload)));

        verify(sendPetitionerCertificateOfEntitlementNotificationEmailTask)
                .execute(contextCaptor.capture(), eq(casePayload));
        verify(sendRespondentCertificateOfEntitlementNotificationEmailTask)
                .execute(contextCaptor.capture(), eq(casePayload));
        verify(sendCoRespondentGenericUpdateNotificationEmailTask)
                .execute(contextCaptor.capture(), eq(casePayload));

        assertThat(contextCaptor.getValue().getTransientObject(CASE_ID_KEY), is(equalTo(CASE_TYPE_ID)));

    }

    @Test
    public void runShouldCallBulkPrintingWhenNoDigitalCommunication() throws Exception {
        Map<String, Object> casePayload = buildCaseData(NO_VALUE);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        when(certificateOfEntitlementLetterGenerationTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(addDocumentToDocumentsToPrintTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(bulkPrinterTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        Map<String, Object> result = caseLinkedForHearingWorkflow.run(buildCaseDetails(casePayload), AUTH_TOKEN);

        assertEquals(result, casePayload);

        InOrder inOrder = inOrder(
            certificateOfEntitlementLetterGenerationTask,
            addDocumentToDocumentsToPrintTask,
            bulkPrinterTask
        );

        inOrder.verify(certificateOfEntitlementLetterGenerationTask).execute(any(TaskContext.class), eq(casePayload));
        inOrder.verify(addDocumentToDocumentsToPrintTask).execute(any(TaskContext.class), eq(casePayload));
        inOrder.verify(bulkPrinterTask).execute(any(TaskContext.class), eq(casePayload));

        verify(sendPetitionerCertificateOfEntitlementNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(sendRespondentCertificateOfEntitlementNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(sendCoRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
    }

    @Test
    public void runShouldSkipBulkPrintingTasksWhenFeatureToggleOff() throws Exception {
        Map<String, Object> casePayload = buildCaseData(NO_VALUE);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);

        Map<String, Object> result = caseLinkedForHearingWorkflow.run(buildCaseDetails(casePayload), AUTH_TOKEN);

        assertEquals(casePayload, result);

        verify(certificateOfEntitlementLetterGenerationTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(addDocumentToDocumentsToPrintTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(bulkPrinterTask, never()).execute(any(TaskContext.class), eq(casePayload));

        verify(sendPetitionerCertificateOfEntitlementNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(sendRespondentCertificateOfEntitlementNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(sendCoRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
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

    private void setupTasksToReturn(Map<String, Object> casePayload) throws TaskException {
        when(sendPetitionerCertificateOfEntitlementNotificationEmailTask.execute(notNull(), eq(casePayload)))
            .thenReturn(casePayload);

        when(sendRespondentCertificateOfEntitlementNotificationEmailTask.execute(notNull(), eq(casePayload)))
            .thenReturn(casePayload);

        when(sendCoRespondentGenericUpdateNotificationEmailTask.execute(notNull(), eq(casePayload)))
            .thenReturn(casePayload);
    }
}
