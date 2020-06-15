package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentSolicitorCoverLetterGenerationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.UNFORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COST_ORDER_CO_RESPONDENT_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_BOTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;

@RunWith(MockitoJUnitRunner.class)
public class SendDnPronouncedNotificationWorkflowTest {

    @Mock
    private SendPetitionerGenericUpdateNotificationEmailTask sendPetitionerGenericUpdateNotificationEmailTask;

    @Mock
    private SendRespondentGenericUpdateNotificationEmailTask sendRespondentGenericUpdateNotificationEmailTask;

    @Mock
    private SendCoRespondentGenericUpdateNotificationEmailTask sendCoRespondentGenericUpdateNotificationEmailTask;

    @Mock
    private CostOrderCoRespondentCoverLetterGenerationTask costOrderCoRespondentCoverLetterGenerationTask;

    @Mock
    private CostOrderCoRespondentSolicitorCoverLetterGenerationTask costOrderCoRespondentSolicitorCoverLetterGenerationTask;

    @Mock
    private FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

    @Mock
    private BulkPrinterTask bulkPrinterTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private SendDnPronouncedNotificationWorkflow sendDnPronouncedNotificationWorkflow;

    private TaskContext context;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
    }

    @Test
    public void genericEmailTaskShouldExecuteAndReturnPayload() throws Exception {
        Map<String, Object> testPayload = ImmutableMap.of("testKey",
            "testValue");

        when(sendPetitionerGenericUpdateNotificationEmailTask.execute(notNull(), eq(testPayload)))
            .thenReturn(testPayload);

        when(sendRespondentGenericUpdateNotificationEmailTask.execute(notNull(), eq(testPayload)))
            .thenReturn(testPayload);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(buildCaseDetails(testPayload), AUTH_TOKEN);
        assertThat(returnedPayload, is(equalTo(testPayload)));

        verify(sendPetitionerGenericUpdateNotificationEmailTask).execute(any(TaskContext.class), eq(testPayload));
        verify(sendRespondentGenericUpdateNotificationEmailTask).execute(any(TaskContext.class), eq(testPayload));
        verify(sendCoRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(testPayload));
    }

    @Test
    public void givenDigitalCaseAndCoRespondentAreLiableForCosts_thenEmailsAreSent() throws Exception {
        Map<String, Object> testPayload = ImmutableMap.of(
            WHO_PAYS_COSTS_CCD_FIELD,
            WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT);

        when(sendPetitionerGenericUpdateNotificationEmailTask.execute(notNull(), eq(testPayload)))
            .thenReturn(testPayload);

        when(sendRespondentGenericUpdateNotificationEmailTask.execute(notNull(), eq(testPayload)))
            .thenReturn(testPayload);

        when(sendCoRespondentGenericUpdateNotificationEmailTask.execute(notNull(), eq(testPayload)))
            .thenReturn(testPayload);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(buildCaseDetails(testPayload), AUTH_TOKEN);
        assertThat(returnedPayload, is(equalTo(testPayload)));

        final InOrder inOrder = inOrder(
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            sendCoRespondentGenericUpdateNotificationEmailTask
        );

        inOrder.verify(sendPetitionerGenericUpdateNotificationEmailTask).execute(any(TaskContext.class), eq(testPayload));
        inOrder.verify(sendRespondentGenericUpdateNotificationEmailTask).execute(any(TaskContext.class), eq(testPayload));
        inOrder.verify(sendCoRespondentGenericUpdateNotificationEmailTask).execute(any(TaskContext.class), eq(testPayload));
    }

    @Test
    public void givenDigitalCaseAndCoRespondentNotLiableForCosts_thenEmailsAreSent() throws Exception {
        Map<String, Object> testPayload = ImmutableMap.of(
            WHO_PAYS_COSTS_CCD_FIELD,
            WHO_PAYS_CCD_CODE_FOR_RESPONDENT);

        when(sendPetitionerGenericUpdateNotificationEmailTask.execute(notNull(), eq(testPayload)))
            .thenReturn(testPayload);

        when(sendRespondentGenericUpdateNotificationEmailTask.execute(notNull(), eq(testPayload)))
            .thenReturn(testPayload);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(buildCaseDetails(testPayload), AUTH_TOKEN);
        assertThat(returnedPayload, is(equalTo(testPayload)));

        final InOrder inOrder = inOrder(
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask
        );

        inOrder.verify(sendPetitionerGenericUpdateNotificationEmailTask).execute(any(TaskContext.class), eq(testPayload));
        inOrder.verify(sendRespondentGenericUpdateNotificationEmailTask).execute(any(TaskContext.class), eq(testPayload));
        verify(sendCoRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(testPayload));
    }

    @Test
    public void givenDigitalCaseAndRespondentAndCoRespondentAreLiableForCosts_thenEmailsAreSent() throws Exception {
        Map<String, Object> testPayload = ImmutableMap.of(
            WHO_PAYS_COSTS_CCD_FIELD,
            WHO_PAYS_CCD_CODE_FOR_BOTH);

        when(sendPetitionerGenericUpdateNotificationEmailTask.execute(notNull(), eq(testPayload)))
            .thenReturn(testPayload);

        when(sendRespondentGenericUpdateNotificationEmailTask.execute(notNull(), eq(testPayload)))
            .thenReturn(testPayload);

        when(sendCoRespondentGenericUpdateNotificationEmailTask.execute(notNull(), eq(testPayload)))
            .thenReturn(testPayload);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(buildCaseDetails(testPayload), AUTH_TOKEN);
        assertThat(returnedPayload, is(equalTo(testPayload)));

        final InOrder inOrder = inOrder(
            sendPetitionerGenericUpdateNotificationEmailTask,
            sendRespondentGenericUpdateNotificationEmailTask,
            sendCoRespondentGenericUpdateNotificationEmailTask
        );

        inOrder.verify(sendPetitionerGenericUpdateNotificationEmailTask).execute(any(TaskContext.class), eq(testPayload));
        inOrder.verify(sendRespondentGenericUpdateNotificationEmailTask).execute(any(TaskContext.class), eq(testPayload));
        inOrder.verify(sendCoRespondentGenericUpdateNotificationEmailTask).execute(any(TaskContext.class), eq(testPayload));
    }

    @Test
    public void givenPaperBased_EmailTasksAreNotCalled() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        Task<Map<String, Object>>[] emptyTasks = getEmptyTaskList();

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.execute(emptyTasks, caseData);
        assertThat(returnedPayload, is(caseData));

        verify(sendPetitionerGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(caseData));
        verify(sendRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(caseData));
        verify(sendCoRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(caseData));
    }

    @Test
    public void givenPaperUpdateToggleIsOff_thenNoBulkPrintTasksAreCalled() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);
        when(costOrderCoRespondentCoverLetterGenerationTask.getDocumentType()).thenReturn(COST_ORDER_CO_RESPONDENT_LETTER_DOCUMENT_TYPE);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(caseDetails, AUTH_TOKEN);
        assertThat(returnedPayload, is(notNullValue()));

        verify(costOrderCoRespondentSolicitorCoverLetterGenerationTask, never()).execute(any(TaskContext.class), eq(caseData));
        verify(costOrderCoRespondentCoverLetterGenerationTask, never()).execute(any(TaskContext.class), eq(caseData));
        verify(fetchPrintDocsFromDmStore, never()).execute(any(TaskContext.class), eq(caseData));
        verify(bulkPrinterTask, never()).execute(any(TaskContext.class), eq(caseData));
    }

    @Test
    public void givenPaperBasedAndPaperUpdateToggledOnAndCostClaimGrantedAndIsNotRepresented_SendCoRespondentCoverLetter() throws Exception {
        Map<String, Object> caseData = buildCaseDataWithCoRespondentAsAddressee();

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        when(costOrderCoRespondentCoverLetterGenerationTask.execute(notNull(), eq(caseData))).thenReturn(caseData);
        when(costOrderCoRespondentCoverLetterGenerationTask.getDocumentType()).thenReturn(COST_ORDER_CO_RESPONDENT_LETTER_DOCUMENT_TYPE);
        when(fetchPrintDocsFromDmStore.execute(notNull(), eq(caseData))).thenReturn(caseData);
        when(bulkPrinterTask.execute(notNull(), eq(caseData))).thenReturn(caseData);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(buildCaseDetails(caseData), AUTH_TOKEN);

        assertThat(returnedPayload, is(caseData));

        final InOrder inOrder = inOrder(
            costOrderCoRespondentCoverLetterGenerationTask,
            fetchPrintDocsFromDmStore,
            bulkPrinterTask
        );

        inOrder.verify(costOrderCoRespondentCoverLetterGenerationTask, times(1)).execute(any(TaskContext.class), eq(caseData));
        inOrder.verify(fetchPrintDocsFromDmStore, times(1)).execute(any(TaskContext.class), eq(caseData));
        inOrder.verify(bulkPrinterTask, times(1)).execute(any(TaskContext.class), eq(caseData));

        verify(sendPetitionerGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(caseData));
        verify(sendRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(caseData));
        verify(sendCoRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), eq(caseData));
        verify(costOrderCoRespondentSolicitorCoverLetterGenerationTask, never()).execute(any(TaskContext.class), eq(caseData));
    }

    @Test
    public void givenPaperBasedAndPaperUpdateToggledOnAndCostClaimGrantedAndIsRepresented_SendCoRespondentSolicitorCoverLetter() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE,
            DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE,
            CO_RESPONDENT_REPRESENTED, YES_VALUE
        );
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(costOrderCoRespondentSolicitorCoverLetterGenerationTask.execute(any(TaskContext.class), eq(caseData))).thenReturn(caseData);
        when(fetchPrintDocsFromDmStore.execute(any(TaskContext.class), eq(caseData))).thenReturn(caseData);
        when(bulkPrinterTask.execute(any(TaskContext.class), eq(caseData))).thenReturn(caseData);
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(caseDetails, AUTH_TOKEN);
        assertThat(returnedPayload, is(notNullValue()));

        verify(sendPetitionerGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), anyMap());
        verify(sendRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), anyMap());
        verify(sendCoRespondentGenericUpdateNotificationEmailTask, never()).execute(any(TaskContext.class), anyMap());


        final InOrder inOrder = inOrder(
            costOrderCoRespondentSolicitorCoverLetterGenerationTask,
            fetchPrintDocsFromDmStore,
            bulkPrinterTask
        );

        inOrder.verify(costOrderCoRespondentSolicitorCoverLetterGenerationTask, times(1)).execute(any(TaskContext.class), eq(caseData));
        inOrder.verify(fetchPrintDocsFromDmStore, times(1)).execute(any(TaskContext.class), eq(caseData));
        inOrder.verify(bulkPrinterTask, times(1)).execute(any(TaskContext.class), eq(caseData));

    }

    @Test
    public void givenPaperBasedAndPaperUpdateToggledOnAndCostClaimNotGranted_thenNoBulkPrintTasksAreCalled() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE,
            DIVORCE_COSTS_CLAIM_CCD_FIELD, NO_VALUE
        );
        CaseDetails caseDetails = buildCaseDetails(caseData);
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(caseDetails, AUTH_TOKEN);
        assertThat(returnedPayload, is(notNullValue()));

        verify(costOrderCoRespondentSolicitorCoverLetterGenerationTask, never()).execute(any(TaskContext.class), eq(caseData));
        verify(costOrderCoRespondentCoverLetterGenerationTask, never()).execute(any(TaskContext.class), eq(caseData));
        verify(bulkPrinterTask, never()).execute(any(TaskContext.class), eq(caseData));
    }

    @Test
    public void givenPaperBasedAndPaperUpdateToggledOff_thenNoBulkPrintTasksAreCalled() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData);
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);

        Map<String, Object> returnedPayload = sendDnPronouncedNotificationWorkflow.run(caseDetails, AUTH_TOKEN);
        assertThat(returnedPayload, is(notNullValue()));

        verify(costOrderCoRespondentSolicitorCoverLetterGenerationTask, never()).execute(any(TaskContext.class), eq(caseData));
        verify(costOrderCoRespondentCoverLetterGenerationTask, never()).execute(any(TaskContext.class), eq(caseData));
        verify(bulkPrinterTask, never()).execute(any(TaskContext.class), eq(caseData));
    }

    private Map<String, Object> buildCaseDataWithCoRespondentAsAddressee() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithCoRespondentAsAddressee();
        caseData.put(D8DOCUMENTS_GENERATED, asList(
            createCollectionMemberDocumentAsMap("http://dn-pronounced.com", COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_TEMPLATE_ID)
        ));
        return caseData;
    }

    private Task<Map<String, Object>>[] getEmptyTaskList() {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        Task<Map<String, Object>>[] arr = new Task[tasks.size()];
        return tasks.toArray(arr);
    }


    private CaseDetails buildCaseDetails(Map<String, Object> casePayload) {
        return CaseDetails.builder()
            .caseId(CASE_TYPE_ID)
            .caseData(casePayload)
            .build();
    }
}
