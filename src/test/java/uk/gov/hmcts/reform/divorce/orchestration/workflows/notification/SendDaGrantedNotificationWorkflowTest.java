package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DaGrantedCitizenLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DaGrantedSolicitorLetterGenerationTask;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_CITIZEN_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocument;

@RunWith(MockitoJUnitRunner.class)
public class SendDaGrantedNotificationWorkflowTest {

    @Mock
    private SendDaGrantedNotificationEmailTask sendDaGrantedNotificationEmailTask;

    @Mock
    private DaGrantedCitizenLetterGenerationTask daGrantedCitizenLetterGenerationTask;

    @Mock
    private DaGrantedSolicitorLetterGenerationTask daGrantedSolicitorLetterGenerationTask;

    @Mock
    private FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;

    @Mock
    private BulkPrinterTask bulkPrinterTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private SendDaGrantedNotificationWorkflow sendDaGrantedNotificationWorkflow;

    @Test
    public void runShouldCallSendDaGrantedNotificationEmailTaskWhenDigitalCommunication() throws Exception {
        Map<String, Object> casePayload = buildCaseData(YES_VALUE);

        when(sendDaGrantedNotificationEmailTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        Map<String, Object> result = sendDaGrantedNotificationWorkflow.run(buildCaseDetails(casePayload), AUTH_TOKEN);

        assertEquals(result, casePayload);

        verify(sendDaGrantedNotificationEmailTask, times(1)).execute(any(TaskContext.class), eq(casePayload));

        verify(daGrantedCitizenLetterGenerationTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(fetchPrintDocsFromDmStoreTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(bulkPrinterTask, never()).execute(any(TaskContext.class), eq(casePayload));
    }

    @Test
    public void runShouldCallBulkPrintingForOfflineRespondent() throws Exception {
        Map<String, Object> incomingCaseData = buildCaseData(NO_VALUE);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        when(daGrantedCitizenLetterGenerationTask.execute(isNotNull(), eq(incomingCaseData))).thenReturn(incomingCaseData);
        when(fetchPrintDocsFromDmStoreTask.execute(isNotNull(), eq(incomingCaseData))).thenReturn(incomingCaseData);
        when(bulkPrinterTask.execute(isNotNull(), eq(incomingCaseData))).thenReturn(incomingCaseData);

        Map<String, Object> returnedCaseData = sendDaGrantedNotificationWorkflow.run(buildCaseDetails(incomingCaseData), AUTH_TOKEN);

        assertEquals(incomingCaseData, returnedCaseData);
        InOrder inOrder = inOrder(
            daGrantedCitizenLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            bulkPrinterTask
        );
        inOrder.verify(daGrantedCitizenLetterGenerationTask).execute(any(TaskContext.class), eq(incomingCaseData));
        inOrder.verify(fetchPrintDocsFromDmStoreTask).execute(any(TaskContext.class), eq(incomingCaseData));
        inOrder.verify(bulkPrinterTask).execute(any(TaskContext.class), eq(incomingCaseData));
        verify(sendDaGrantedNotificationEmailTask, never()).execute(any(TaskContext.class), eq(incomingCaseData));
    }

    @Test
    public void runShouldCallBulkPrintingForOfflineRepresentedRespondent() throws Exception {
        Map<String, Object> incomingCaseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCaseData(NO_VALUE))
            .put(RESP_SOL_REPRESENTED, YES_VALUE)
            .build();

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        when(daGrantedSolicitorLetterGenerationTask.execute(isNotNull(), eq(incomingCaseData))).thenReturn(incomingCaseData);
        when(fetchPrintDocsFromDmStoreTask.execute(isNotNull(), eq(incomingCaseData))).thenReturn(incomingCaseData);
        when(bulkPrinterTask.execute(isNotNull(), eq(incomingCaseData))).thenReturn(incomingCaseData);

        Map<String, Object> returnedCaseData = sendDaGrantedNotificationWorkflow.run(buildCaseDetails(incomingCaseData), AUTH_TOKEN);

        assertEquals(incomingCaseData, returnedCaseData);
        InOrder inOrder = inOrder(
            daGrantedSolicitorLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            bulkPrinterTask
        );
        inOrder.verify(daGrantedSolicitorLetterGenerationTask).execute(any(TaskContext.class), eq(incomingCaseData));
        inOrder.verify(fetchPrintDocsFromDmStoreTask).execute(any(TaskContext.class), eq(incomingCaseData));
        inOrder.verify(bulkPrinterTask).execute(any(TaskContext.class), eq(incomingCaseData));
        verify(sendDaGrantedNotificationEmailTask, never()).execute(any(TaskContext.class), eq(incomingCaseData));
    }

    @Test
    public void runRemoveDaGrantedLetterFromCaseData() throws Exception {
        Map<String, Object> incomingCaseData = ImmutableMap.of(
            RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE,
            D8DOCUMENTS_GENERATED, asList(
                createCollectionMemberDocument("http://daGrantedLetter.com", DECREE_ABSOLUTE_GRANTED_CITIZEN_LETTER_DOCUMENT_TYPE, "daGrantedLetter.pdf"),
                createCollectionMemberDocument("http://daGrantedSolicitorLetter.com", DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER_DOCUMENT_TYPE, "daGrantedSolicitorLetter.pdf")
            )
        );

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        when(daGrantedCitizenLetterGenerationTask.getDocumentType()).thenReturn(DECREE_ABSOLUTE_GRANTED_CITIZEN_LETTER_DOCUMENT_TYPE);
        when(daGrantedSolicitorLetterGenerationTask.getDocumentType()).thenReturn(DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER_DOCUMENT_TYPE);

        when(daGrantedCitizenLetterGenerationTask.execute(isNotNull(), eq(incomingCaseData))).thenReturn(incomingCaseData);
        when(fetchPrintDocsFromDmStoreTask.execute(isNotNull(), eq(incomingCaseData))).thenReturn(incomingCaseData);
        when(bulkPrinterTask.execute(isNotNull(), eq(incomingCaseData))).thenReturn(incomingCaseData);

        Map<String, Object> returnedCaseData = sendDaGrantedNotificationWorkflow.run(buildCaseDetails(incomingCaseData), AUTH_TOKEN);

        assertThat(returnedCaseData.get(D8DOCUMENTS_GENERATED), is(nullValue()));
        InOrder inOrder = inOrder(
            daGrantedCitizenLetterGenerationTask,
            fetchPrintDocsFromDmStoreTask,
            bulkPrinterTask
        );
        inOrder.verify(daGrantedCitizenLetterGenerationTask).execute(any(TaskContext.class), eq(incomingCaseData));
        inOrder.verify(fetchPrintDocsFromDmStoreTask).execute(any(TaskContext.class), eq(incomingCaseData));
        inOrder.verify(bulkPrinterTask).execute(any(TaskContext.class), eq(incomingCaseData));
        verify(sendDaGrantedNotificationEmailTask, never()).execute(any(TaskContext.class), eq(incomingCaseData));
    }

    @Test
    public void runShouldSkipBulkPrintingTasksWhenFeatureToggleOff() throws Exception {
        Map<String, Object> casePayload = buildCaseData(NO_VALUE);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);

        Map<String, Object> result = sendDaGrantedNotificationWorkflow.run(buildCaseDetails(casePayload), AUTH_TOKEN);

        assertEquals(casePayload, result);

        verify(daGrantedCitizenLetterGenerationTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(fetchPrintDocsFromDmStoreTask, never()).execute(any(TaskContext.class), eq(casePayload));
        verify(bulkPrinterTask, never()).execute(any(TaskContext.class), eq(casePayload));

        verify(sendDaGrantedNotificationEmailTask, never()).execute(any(TaskContext.class), eq(casePayload));
    }

    private Map<String, Object> buildCaseData(String value) {
        return ImmutableMap.of(
            RESP_IS_USING_DIGITAL_CHANNEL, value,
            D8DOCUMENTS_GENERATED, asList(createCollectionMemberDocument("http://daGranted.com", DECREE_ABSOLUTE_DOCUMENT_TYPE, DECREE_ABSOLUTE_FILENAME))
        );
    }

    private CaseDetails buildCaseDetails(Map<String, Object> casePayload) {
        return CaseDetails.builder()
            .caseId(CASE_TYPE_ID)
            .caseData(casePayload)
            .build();
    }

}