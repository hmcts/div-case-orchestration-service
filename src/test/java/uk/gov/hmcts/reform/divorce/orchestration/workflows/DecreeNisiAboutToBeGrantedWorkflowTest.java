package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDecreeNisiDecisionDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDnOutcomeFlagFieldTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DefineWhoPaysCostsOrderTask;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
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
    private  AddDnOutcomeFlagFieldTask addDnOutcomeFlagFieldTask;

    @Mock
    private SetDNDecisionStateTask setDNDecisionStateTask;

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
        when(setDNDecisionStateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(validateDNDecisionTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(addDecreeNisiDecisionDateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);
        when(defineWhoPaysCostsOrderTask.execute(isNotNull(), eq(payloadReturnedByTask))).thenReturn(payloadReturnedByTask);
        when(addDnOutcomeFlagFieldTask.execute(isNotNull(), eq(payloadReturnedByTask))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build());

        assertThat(returnedPayload, allOf(
            hasEntry(equalTo("addedKey"), equalTo("addedValue")),
            hasEntry(equalTo(DECREE_NISI_GRANTED_CCD_FIELD), equalTo(YES_VALUE))
        ));

        final InOrder inOrder = inOrder(
            setDNDecisionStateTask,
            validateDNDecisionTask,
            addDecreeNisiDecisionDateTask,
            defineWhoPaysCostsOrderTask,
            addDnOutcomeFlagFieldTask
        );

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
        when(setDNDecisionStateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(validateDNDecisionTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(addDecreeNisiDecisionDateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);
        when(addDnOutcomeFlagFieldTask.execute(isNotNull(), eq(payloadReturnedByTask))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build());

        assertThat(returnedPayload, allOf(
            hasEntry(equalTo("addedKey"), equalTo("addedValue")),
            hasEntry(equalTo(DECREE_NISI_GRANTED_CCD_FIELD), equalTo(YES_VALUE))
        ));

        final InOrder inOrder = inOrder(
            setDNDecisionStateTask,
            validateDNDecisionTask,
            addDecreeNisiDecisionDateTask,
            addDnOutcomeFlagFieldTask
        );

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
        when(setDNDecisionStateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(validateDNDecisionTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(addDecreeNisiDecisionDateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build());

        assertThat(returnedPayload, allOf(
            hasEntry(equalTo(DECREE_NISI_GRANTED_CCD_FIELD), equalTo(NO_VALUE))
        ));

        final InOrder inOrder = inOrder(
            setDNDecisionStateTask,
            validateDNDecisionTask,
            addDecreeNisiDecisionDateTask
        );

        inOrder.verify(setDNDecisionStateTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(validateDNDecisionTask).execute(any(TaskContext.class), eq(inputPayload));
        inOrder.verify(addDecreeNisiDecisionDateTask).execute(any(TaskContext.class), eq(inputPayload));

        verify(addDnOutcomeFlagFieldTask, never()).execute(any(), any());
        verify(defineWhoPaysCostsOrderTask, never()).execute(any(), any());
    }

    @Test
    public void givenValidationException_whenDNAboutToBeGranted_thenReturnWorkflowException() throws TaskException {
        final String errorMessage = "TaskException";
        inputPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        when(setDNDecisionStateTask.execute(isNotNull(), eq(inputPayload))).thenReturn(inputPayload);
        when(validateDNDecisionTask.execute(isNotNull(), eq(inputPayload))).thenThrow(new TaskException(errorMessage));

        try {
            workflow.run(CaseDetails.builder().caseData(inputPayload).build());
            fail("Workflow exception expected");
        } catch (WorkflowException e) {
            assertThat(e.getMessage(), is(errorMessage));
        }

        verify(addDecreeNisiDecisionDateTask, never()).execute(any(), any());
        verify(addDnOutcomeFlagFieldTask, never()).execute(any(), any());
    }
}
