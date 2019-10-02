package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDecreeNisiDecisionDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDnOutcomeFlagFieldTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DecreeNisiRefusalDocumentGeneratorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DefineWhoPaysCostsOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDnDecisionNotificationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDNDecisionStateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateDNDecisionTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class DecreeNisiAboutToBeGrantedWorkflowTest {

    private static final String DECREE_NISI_GRANTED_CCD_FIELD = "DecreeNisiGranted";
    private static final String COSTS_CLAIM_GRANTED = "CostsClaimGranted";

    @Mock
    private ValidateDNDecisionTask validateDNDecisionTask;

    @Mock
    private AddDecreeNisiDecisionDateTask addDecreeNisiDecisionDateTask;

    @Mock
    private DefineWhoPaysCostsOrderTask defineWhoPaysCostsOrderTask;

    @Mock
    private AddDnOutcomeFlagFieldTask addDnOutcomeFlagFieldTask;

    @Mock
    private SetDNDecisionStateTask setDNDecisionStateTask;

    @Mock
    private DecreeNisiRefusalDocumentGeneratorTask decreeNisiRefusalDocumentGeneratorTask;

    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Mock
    private SendDnDecisionNotificationTask sendDnDecisionNotificationTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private DecreeNisiAboutToBeGrantedWorkflow workflow;

    private Map<String, Object> inputPayload;

    @Before
    public void setUp() {
        inputPayload = new HashMap<>();
    }

    @Test
    public void shouldCallTasksAccordingly_IfDecreeNisiIsGranted_AndCostsOrderIsGranted() throws WorkflowException, TaskException {
        inputPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE);
        inputPayload.put(COSTS_CLAIM_GRANTED, YES_VALUE);

        Map<String, Object> payloadReturnedByTask = new HashMap<>(inputPayload);
        payloadReturnedByTask.put("addedKey", "addedValue");
        when(featureToggleService.isFeatureEnabled(eq(Features.DN_REFUSAL))).thenReturn(false);
        when(setDNDecisionStateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(validateDNDecisionTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(addDecreeNisiDecisionDateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);
        when(defineWhoPaysCostsOrderTask.execute(isNotNull(), eq(payloadReturnedByTask))).thenReturn(payloadReturnedByTask);
        when(addDnOutcomeFlagFieldTask.execute(isNotNull(), eq(payloadReturnedByTask))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build(), AUTH_TOKEN);

        assertThat(returnedPayload, allOf(
            hasEntry(equalTo("addedKey"), equalTo("addedValue")),
            hasEntry(equalTo(DECREE_NISI_GRANTED_CCD_FIELD), equalTo(YES_VALUE))
        ));

        final InOrder inOrder = inOrder(
            featureToggleService,
            setDNDecisionStateTask,
            validateDNDecisionTask,
            addDecreeNisiDecisionDateTask,
            addDnOutcomeFlagFieldTask,
            defineWhoPaysCostsOrderTask
        );

        inOrder.verify(featureToggleService).isFeatureEnabled(eq(Features.DN_REFUSAL));
        inOrder.verify(setDNDecisionStateTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(validateDNDecisionTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(addDecreeNisiDecisionDateTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(addDnOutcomeFlagFieldTask).execute(any(TaskContext.class), eq(payloadReturnedByTask));
        inOrder.verify(defineWhoPaysCostsOrderTask).execute(any(TaskContext.class), eq(payloadReturnedByTask));
    }

    @Test
    public void shouldCallTasksAccordingly_IfDecreeNisiIsGranted_ButCostsOrderIsNotGranted() throws WorkflowException, TaskException {
        inputPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE);
        inputPayload.put(COSTS_CLAIM_GRANTED, NO_VALUE);

        Map<String, Object> payloadReturnedByTask = new HashMap<>(inputPayload);
        payloadReturnedByTask.put("addedKey", "addedValue");
        when(featureToggleService.isFeatureEnabled(eq(Features.DN_REFUSAL))).thenReturn(false);
        when(setDNDecisionStateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(validateDNDecisionTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(addDecreeNisiDecisionDateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);
        when(addDnOutcomeFlagFieldTask.execute(isNotNull(), eq(payloadReturnedByTask))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build(), AUTH_TOKEN);

        assertThat(returnedPayload, allOf(
            hasEntry(equalTo("addedKey"), equalTo("addedValue")),
            hasEntry(equalTo(DECREE_NISI_GRANTED_CCD_FIELD), equalTo(YES_VALUE))
        ));

        final InOrder inOrder = inOrder(
            featureToggleService,
            setDNDecisionStateTask,
            validateDNDecisionTask,
            addDecreeNisiDecisionDateTask,
            addDnOutcomeFlagFieldTask
        );

        inOrder.verify(featureToggleService).isFeatureEnabled(eq(Features.DN_REFUSAL));
        inOrder.verify(setDNDecisionStateTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(validateDNDecisionTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(addDecreeNisiDecisionDateTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(addDnOutcomeFlagFieldTask).execute(any(TaskContext.class), eq(payloadReturnedByTask));

        verify(defineWhoPaysCostsOrderTask, never()).execute(any(), any());
    }

    @Test
    public void shouldCallTasksAccordingly_IfDecreeNisiIsNotGranted() throws WorkflowException, TaskException {
        inputPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        Map<String, Object> payloadReturnedByTask = new HashMap<>(inputPayload);
        when(featureToggleService.isFeatureEnabled(eq(Features.DN_REFUSAL))).thenReturn(false);
        when(setDNDecisionStateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(validateDNDecisionTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(addDecreeNisiDecisionDateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build(), AUTH_TOKEN);

        assertThat(returnedPayload, allOf(
            hasEntry(equalTo(DECREE_NISI_GRANTED_CCD_FIELD), equalTo(NO_VALUE))
        ));

        final InOrder inOrder = inOrder(
            featureToggleService,
            setDNDecisionStateTask,
            validateDNDecisionTask,
            addDecreeNisiDecisionDateTask
        );

        inOrder.verify(featureToggleService).isFeatureEnabled(eq(Features.DN_REFUSAL));
        inOrder.verify(setDNDecisionStateTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(validateDNDecisionTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(addDecreeNisiDecisionDateTask).execute(any(TaskContext.class), eq(inputPayload));

        verify(addDnOutcomeFlagFieldTask, never()).execute(any(), any());
        verify(defineWhoPaysCostsOrderTask, never()).execute(any(), any());
    }

    @Test
    public void shouldCallTasksAccordingly_IfDecreeNisiIsRefusedWithoutMoreInfo() throws WorkflowException, TaskException {
        inputPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        inputPayload.put(REFUSAL_DECISION_CCD_FIELD, "NotDetermined");
        Map<String, Object> payloadReturnedByTask = new HashMap<>(inputPayload);
        when(featureToggleService.isFeatureEnabled(eq(Features.DN_REFUSAL))).thenReturn(false);
        when(setDNDecisionStateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(validateDNDecisionTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(addDecreeNisiDecisionDateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build(), AUTH_TOKEN);

        assertThat(returnedPayload, allOf(
            hasEntry(equalTo(DECREE_NISI_GRANTED_CCD_FIELD), equalTo(NO_VALUE))
        ));

        final InOrder inOrder = inOrder(
            featureToggleService,
            setDNDecisionStateTask,
            validateDNDecisionTask,
            addDecreeNisiDecisionDateTask
        );

        inOrder.verify(featureToggleService).isFeatureEnabled(eq(Features.DN_REFUSAL));
        inOrder.verify(setDNDecisionStateTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(validateDNDecisionTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(addDecreeNisiDecisionDateTask).execute(any(TaskContext.class), eq(inputPayload));

        verify(addDnOutcomeFlagFieldTask, never()).execute(any(), any());
        verify(defineWhoPaysCostsOrderTask, never()).execute(any(), any());
        verify(decreeNisiRefusalDocumentGeneratorTask, never()).execute(any(), any());
        verify(caseFormatterAddDocuments, never()).execute(any(), any());
    }

    @Test
    public void shouldCallTasksAccordingly_IfDecreeNisiIsRefusedWithMoreInfoAndFeatureToggleIsOn() throws WorkflowException, TaskException {
        inputPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        inputPayload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);
        Map<String, Object> payloadReturnedByTask = new HashMap<>(inputPayload);
        when(featureToggleService.isFeatureEnabled(eq(Features.DN_REFUSAL))).thenReturn(true);
        when(setDNDecisionStateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(validateDNDecisionTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(addDecreeNisiDecisionDateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);
        when(decreeNisiRefusalDocumentGeneratorTask.execute(isNotNull(), eq(payloadReturnedByTask))).thenReturn(payloadReturnedByTask);
        when(caseFormatterAddDocuments.execute(isNotNull(), eq(payloadReturnedByTask))).thenReturn(payloadReturnedByTask);
        when(sendDnDecisionNotificationTask.execute(isNotNull(), eq(payloadReturnedByTask))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build(), AUTH_TOKEN);

        assertThat(returnedPayload, allOf(
                hasEntry(equalTo(DECREE_NISI_GRANTED_CCD_FIELD), equalTo(NO_VALUE))
        ));

        final InOrder inOrder = inOrder(
            featureToggleService,
            setDNDecisionStateTask,
            validateDNDecisionTask,
            addDecreeNisiDecisionDateTask,
            decreeNisiRefusalDocumentGeneratorTask,
            caseFormatterAddDocuments,
            sendDnDecisionNotificationTask
        );

        inOrder.verify(featureToggleService).isFeatureEnabled(eq(Features.DN_REFUSAL));
        inOrder.verify(setDNDecisionStateTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(validateDNDecisionTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(addDecreeNisiDecisionDateTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(decreeNisiRefusalDocumentGeneratorTask).execute(any(TaskContext.class), eq(payloadReturnedByTask));
        inOrder.verify(caseFormatterAddDocuments).execute(any(TaskContext.class), eq(payloadReturnedByTask));
        inOrder.verify(sendDnDecisionNotificationTask).execute(any(TaskContext.class), eq(payloadReturnedByTask));

        verify(addDnOutcomeFlagFieldTask, never()).execute(any(), any());
        verify(defineWhoPaysCostsOrderTask, never()).execute(any(), any());
    }

    @Test
    public void shouldCallTasksAccordingly_IfDecreeNisiIsRefusedWithMoreInfoAndFeatureToggleIsOff() throws WorkflowException, TaskException {
        inputPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        inputPayload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);
        Map<String, Object> payloadReturnedByTask = new HashMap<>(inputPayload);
        when(featureToggleService.isFeatureEnabled(eq(Features.DN_REFUSAL))).thenReturn(false);
        when(setDNDecisionStateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(validateDNDecisionTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(addDecreeNisiDecisionDateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build(), AUTH_TOKEN);

        assertThat(returnedPayload, allOf(
            hasEntry(equalTo(DECREE_NISI_GRANTED_CCD_FIELD), equalTo(NO_VALUE))
        ));

        final InOrder inOrder = inOrder(
            featureToggleService,
            setDNDecisionStateTask,
            validateDNDecisionTask,
            addDecreeNisiDecisionDateTask
        );

        inOrder.verify(featureToggleService).isFeatureEnabled(eq(Features.DN_REFUSAL));
        inOrder.verify(setDNDecisionStateTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(validateDNDecisionTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(addDecreeNisiDecisionDateTask).execute(any(TaskContext.class), eq(inputPayload));

        verify(addDnOutcomeFlagFieldTask, never()).execute(any(), any());
        verify(defineWhoPaysCostsOrderTask, never()).execute(any(), any());
        verify(decreeNisiRefusalDocumentGeneratorTask, never()).execute(any(), any());
        verify(caseFormatterAddDocuments, never()).execute(any(), any());
    }

    @Test
    public void givenValidationException_whenDNAboutToBeGranted_thenReturnWorkflowException() throws TaskException {
        final String errorMessage = "TaskException";
        inputPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        when(setDNDecisionStateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(validateDNDecisionTask.execute(isNotNull(), eq(inputPayload))).thenThrow(new TaskException(errorMessage));

        try {
            workflow.run(CaseDetails.builder().caseData(inputPayload).build(), AUTH_TOKEN);
            fail("Workflow exception expected");
        } catch (WorkflowException e) {
            assertThat(e.getMessage(), is(errorMessage));
        }

        verify(addDecreeNisiDecisionDateTask, never()).execute(any(), any());
        verify(addDnOutcomeFlagFieldTask, never()).execute(any(), any());
    }
}
