package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCertificateOfEntitlementNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentCertificateOfEntitlementNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CertificateOfEntitlementLetterGenerationTask;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;

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
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Mock
    private FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

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

        verify(certificateOfEntitlementLetterGenerationTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(caseFormatterAddDocuments, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(fetchPrintDocsFromDmStore, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(bulkPrinterTask, never()).execute(any(TaskContext.class), eq(casePayload));

        assertThat(contextCaptor.getValue().getTransientObject(CASE_ID_JSON_KEY), is(equalTo(CASE_TYPE_ID)));

    }

    @Test
    public void runShouldCallBulkPrintingWhenNoDigitalCommunication() throws Exception {
        Map<String, Object> casePayload = buildCaseData(NO_VALUE);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        when(certificateOfEntitlementLetterGenerationTask.getDocumentType()).thenReturn(CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE);
        when(certificateOfEntitlementLetterGenerationTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(caseFormatterAddDocuments.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(fetchPrintDocsFromDmStore.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(bulkPrinterTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        Map<String, Object> result = caseLinkedForHearingWorkflow.run(buildCaseDetails(casePayload), AUTH_TOKEN);

        assertEquals(result, casePayload);

        InOrder inOrder = inOrder(
            certificateOfEntitlementLetterGenerationTask,
            caseFormatterAddDocuments,
            fetchPrintDocsFromDmStore,
            bulkPrinterTask
        );

        inOrder.verify(certificateOfEntitlementLetterGenerationTask).execute(any(TaskContext.class), eq(casePayload));
        inOrder.verify(caseFormatterAddDocuments).execute(any(TaskContext.class), eq(casePayload));
        inOrder.verify(fetchPrintDocsFromDmStore).execute(any(TaskContext.class), eq(casePayload));
        inOrder.verify(bulkPrinterTask).execute(any(TaskContext.class), eq(casePayload));

        verify(sendPetitionerCertificateOfEntitlementNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(sendRespondentCertificateOfEntitlementNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(sendCoRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
    }

    @Test
    public void runRemoveCertificateOfEntitlementLetterFromCaseData() throws Exception {
        Map<String, Object> casePayload = ImmutableMap.of(
            RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE,
            D8DOCUMENTS_GENERATED, asList(createCollectionMemberDocumentAsMap("http://coeLetter.com", CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE, "certificateOfEntitlementCoverLetterForRespondent.pdf"))
        );

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        when(certificateOfEntitlementLetterGenerationTask.getDocumentType()).thenReturn(CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE);
        when(certificateOfEntitlementLetterGenerationTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(caseFormatterAddDocuments.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(fetchPrintDocsFromDmStore.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(bulkPrinterTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        Map<String, Object> returnedCaseData = caseLinkedForHearingWorkflow.run(buildCaseDetails(casePayload), AUTH_TOKEN);

        assertThat(returnedCaseData.get(D8DOCUMENTS_GENERATED), Is.is(emptyList()));
        InOrder inOrder = inOrder(
            certificateOfEntitlementLetterGenerationTask,
            caseFormatterAddDocuments,
            fetchPrintDocsFromDmStore,
            bulkPrinterTask
        );
        inOrder.verify(certificateOfEntitlementLetterGenerationTask).execute(any(TaskContext.class), eq(casePayload));
        inOrder.verify(caseFormatterAddDocuments).execute(any(TaskContext.class), eq(casePayload));
        inOrder.verify(fetchPrintDocsFromDmStore).execute(any(TaskContext.class), eq(casePayload));
        inOrder.verify(bulkPrinterTask).execute(any(TaskContext.class), eq(casePayload));

        verify(sendPetitionerCertificateOfEntitlementNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(sendRespondentCertificateOfEntitlementNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(sendCoRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
    }

    @Test
    public void runShouldSkipBulkPrintingTasksWhenFeatureToggleOff() throws Exception {
        Map<String, Object> casePayload = buildCaseData(NO_VALUE);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);
        when(certificateOfEntitlementLetterGenerationTask.getDocumentType()).thenReturn(CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE);

        Map<String, Object> result = caseLinkedForHearingWorkflow.run(buildCaseDetails(casePayload), AUTH_TOKEN);

        assertEquals(casePayload, result);

        verify(certificateOfEntitlementLetterGenerationTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(caseFormatterAddDocuments, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(fetchPrintDocsFromDmStore, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(bulkPrinterTask, never()).execute(any(TaskContext.class), eq(casePayload));

        verify(sendPetitionerCertificateOfEntitlementNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(sendRespondentCertificateOfEntitlementNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(sendCoRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
    }

    private Map<String, Object> buildCaseData(String value) {
        return ImmutableMap.of(
            RESP_IS_USING_DIGITAL_CHANNEL, value,
            D8DOCUMENTS_GENERATED, asList(createCollectionMemberDocumentAsMap("http://certificateOfEntitlement.com", CERTIFICATE_OF_ENTITLEMENT_DOCUMENT_TYPE, CERTIFICATE_OF_ENTITLEMENT_FILENAME))
        );
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
        when(certificateOfEntitlementLetterGenerationTask.getDocumentType()).thenReturn(CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE);
    }
}
