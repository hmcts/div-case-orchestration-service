package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
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
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCoENotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentCoENotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoECoRespondentCoverLetterGenerationTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class CaseLinkedForHearingWorkflowTest {

    public static final String TEST_CASE_ID = "testCaseId";

    @Mock
    private SendPetitionerCoENotificationEmailTask sendPetitionerCoENotificationEmailTask;

    @Mock
    private SendRespondentCoENotificationEmailTask sendRespondentCoENotificationEmailTask;

    @Mock
    private SendCoRespondentGenericUpdateNotificationEmailTask sendCoRespondentGenericUpdateNotificationEmailTask;

    @Mock
    private CoECoRespondentCoverLetterGenerationTask coECoRespondentCoverLetterGenerationTask;

    @Mock
    private FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

    @Mock
    private BulkPrinterTask bulkPrinterTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CaseLinkedForHearingWorkflow caseLinkedForHearingWorkflow;

    @Captor
    private ArgumentCaptor<TaskContext> contextCaptor;

    private Map<String, Object> payload = new HashMap<>(ImmutableMap.of("testKey", "testValue"));

    @Before
    public void setUp() throws TaskException {
        when(sendPetitionerCoENotificationEmailTask.execute(notNull(), eq(payload))).thenReturn(payload);
        when(sendRespondentCoENotificationEmailTask.execute(notNull(), eq(payload))).thenReturn(payload);
        when(sendCoRespondentGenericUpdateNotificationEmailTask.execute(notNull(), eq(payload))).thenReturn(payload);
        when(coECoRespondentCoverLetterGenerationTask.execute(notNull(), eq(payload))).thenReturn(payload);
        when(fetchPrintDocsFromDmStore.execute(notNull(), eq(payload))).thenReturn(payload);
        when(bulkPrinterTask.execute(notNull(), eq(payload))).thenReturn(payload);
    }

    @Test
    public void sendOnlyEmailsWhenCoRespondentIsUsingDigitalChannel() throws TaskException, WorkflowException {
        CaseDetails caseDetails = createCaseDetails(payload, true);

        Map<String, Object> returnedPayload = caseLinkedForHearingWorkflow.run(caseDetails, AUTH_TOKEN);

        assertThat(returnedPayload, is(equalTo(payload)));

        final InOrder inOrder = inOrder(
            sendPetitionerCoENotificationEmailTask,
            sendRespondentCoENotificationEmailTask,
            sendCoRespondentGenericUpdateNotificationEmailTask
        );

        inOrder.verify(sendPetitionerCoENotificationEmailTask).execute(contextCaptor.capture(), eq(payload));
        inOrder.verify(sendRespondentCoENotificationEmailTask).execute(contextCaptor.capture(), eq(payload));
        inOrder.verify(sendCoRespondentGenericUpdateNotificationEmailTask).execute(contextCaptor.capture(), eq(payload));

        verifyNotCalled(coECoRespondentCoverLetterGenerationTask);
        verifyNotCalled(fetchPrintDocsFromDmStore);
        verifyNotCalled(bulkPrinterTask);

        assertThat(contextCaptor.getValue().getTransientObject(CASE_ID_JSON_KEY), is(equalTo(TEST_CASE_ID)));
    }

    @Test
    public void sendEmailsToPetitionerAndRespondentWhenCoRespondentIsNotUsingDigitalChannel() throws Exception {
        CaseDetails caseDetails = createCaseDetails(payload, false);

        Map<String, Object> returnedPayload = caseLinkedForHearingWorkflow.run(caseDetails, AUTH_TOKEN);

        assertThat(returnedPayload, is(equalTo(payload)));

        final InOrder inOrder = inOrder(
            sendPetitionerCoENotificationEmailTask,
            sendRespondentCoENotificationEmailTask
        );

        inOrder.verify(sendPetitionerCoENotificationEmailTask).execute(contextCaptor.capture(), eq(payload));
        inOrder.verify(sendRespondentCoENotificationEmailTask).execute(contextCaptor.capture(), eq(payload));
        verifyNotCalled(sendCoRespondentGenericUpdateNotificationEmailTask);
        assertThat(contextCaptor.getValue().getTransientObject(CASE_ID_JSON_KEY), is(equalTo(TEST_CASE_ID)));
    }

    @Test
    public void sendEmailsToPetitionerAndRespondentWhenPaperUpdateFeatureToggledOff() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        CaseDetails caseDetails = createCaseDetails(payload, false);

        Map<String, Object> returnedPayload = caseLinkedForHearingWorkflow.run(caseDetails, AUTH_TOKEN);

        assertThat(returnedPayload, is(equalTo(payload)));

        final InOrder inOrder = inOrder(
            sendPetitionerCoENotificationEmailTask,
            sendRespondentCoENotificationEmailTask,
            coECoRespondentCoverLetterGenerationTask,
            fetchPrintDocsFromDmStore,
            bulkPrinterTask
        );

        inOrder.verify(sendPetitionerCoENotificationEmailTask).execute(contextCaptor.capture(), eq(payload));
        inOrder.verify(sendRespondentCoENotificationEmailTask).execute(contextCaptor.capture(), eq(payload));
        inOrder.verify(coECoRespondentCoverLetterGenerationTask).execute(contextCaptor.capture(), eq(payload));
        inOrder.verify(fetchPrintDocsFromDmStore).execute(contextCaptor.capture(), eq(payload));
        inOrder.verify(bulkPrinterTask).execute(contextCaptor.capture(), eq(payload));

        assertThat(contextCaptor.getValue().getTransientObject(CASE_ID_JSON_KEY), is(equalTo(TEST_CASE_ID)));
        verifyNotCalled(sendCoRespondentGenericUpdateNotificationEmailTask);
    }

    private void verifyNotCalled(Task<Map<String, Object>> task) throws TaskException {
        verify(task, times(0)).execute(contextCaptor.capture(), anyMap());
    }

    private CaseDetails createCaseDetails(Map<String, Object> testPayload, boolean coRespContactMethodIsDigital) {
        testPayload.put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, coRespContactMethodIsDigital ? YES_VALUE : NO_VALUE);

        return CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(testPayload)
            .build();
    }
}
