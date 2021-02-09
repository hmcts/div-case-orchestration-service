package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddCourtsToPayloadTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataDraftToDivorceFormatterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetInconsistentPaymentInfoTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetCaseIdAndStateOnSession;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdatePaymentMadeCase;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveDraftWorkflowTest {

    @Mock
    private RetrieveDraftTask retrieveDraftTask;

    @Mock
    private CaseDataDraftToDivorceFormatterTask caseDataToDivorceFormatter;

    @Mock
    private SetCaseIdAndStateOnSession setCaseIdAndStateOnSession;

    @Mock
    private AddCourtsToPayloadTask addCourtsToPayloadTask;

    @Mock
    private GetInconsistentPaymentInfoTask getInconsistentPaymentInfoTask;

    @Mock
    private UpdatePaymentMadeCase paymentMadeEvent;

    @Mock
    private FormatDivorceSessionToCaseDataTask formatDivorceSessionToCaseDataTask;

    @InjectMocks
    private RetrieveDraftWorkflow target;

    @SuppressWarnings("unchecked")
    @Test
    public void givenDraft_whenExecuteGetDraftWorkflow_thenExecuteGetTaskInOrder() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> casePayload = mock(Map.class);
        Map<String, Object> draftPayload = mock(Map.class);

        ArgumentMatcher<TaskContext> contextWithAuthTokenMatcher =
            argument -> argument.getTransientObject(AUTH_TOKEN_JSON_KEY) != null;

        when(retrieveDraftTask.execute(argThat(contextWithAuthTokenMatcher), eq(payload))).thenReturn(casePayload);
        when(getInconsistentPaymentInfoTask.execute(argThat(contextWithAuthTokenMatcher),
            eq(casePayload))).thenAnswer(invocation ->  {
                TaskContext context = invocation.getArgument(0);
                context.setTaskFailed(true);
                return null;
            }
        );

        when(caseDataToDivorceFormatter.execute(argThat(contextWithAuthTokenMatcher),
                eq(casePayload))).thenReturn(draftPayload);
        when(setCaseIdAndStateOnSession.execute(argThat(contextWithAuthTokenMatcher),
                eq(draftPayload))).thenReturn(draftPayload);
        when(addCourtsToPayloadTask.execute(argThat(contextWithAuthTokenMatcher),
            eq(draftPayload))).thenReturn(draftPayload);
        assertEquals(draftPayload, target.run(AUTH_TOKEN));

        verify(retrieveDraftTask).execute(argThat(contextWithAuthTokenMatcher),eq(payload));
        verify(getInconsistentPaymentInfoTask).execute(argThat(contextWithAuthTokenMatcher),eq(casePayload));
        verify(caseDataToDivorceFormatter).execute(argThat(contextWithAuthTokenMatcher),eq(casePayload));
        verify(setCaseIdAndStateOnSession).execute(argThat(contextWithAuthTokenMatcher),eq(draftPayload));
        verify(addCourtsToPayloadTask).execute(argThat(contextWithAuthTokenMatcher),eq(draftPayload));
        verify(paymentMadeEvent, never()).execute(any(),any());
        verify(formatDivorceSessionToCaseDataTask, never()).execute(any(),any());
    }

    @Test
    public void givenDraftWithPaymentDone_whenExecuteWorkflow_thenExecuteGetTaskInOrder() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> casePayload = mock(Map.class);
        Map<String, Object> draftPayload = mock(Map.class);
        Map<String, Object> paymentPayload = mock(Map.class);

        ArgumentMatcher<TaskContext> contextWithAuthTokenMatcher =
            argument -> argument.getTransientObject(AUTH_TOKEN_JSON_KEY) != null;

        when(retrieveDraftTask.execute(argThat(contextWithAuthTokenMatcher), eq(payload))).thenReturn(casePayload);
        when(getInconsistentPaymentInfoTask.execute(argThat(contextWithAuthTokenMatcher),
            eq(casePayload))).thenReturn(paymentPayload);
        when(formatDivorceSessionToCaseDataTask.execute(argThat(contextWithAuthTokenMatcher),
            eq(paymentPayload))).thenReturn(paymentPayload);
        when(paymentMadeEvent.execute(argThat(contextWithAuthTokenMatcher),
            eq(paymentPayload))).thenReturn(paymentPayload);

        when(caseDataToDivorceFormatter.execute(argThat(contextWithAuthTokenMatcher),
            eq(casePayload))).thenReturn(draftPayload);
        when(setCaseIdAndStateOnSession.execute(argThat(contextWithAuthTokenMatcher),
            eq(draftPayload))).thenReturn(draftPayload);
        when(addCourtsToPayloadTask.execute(argThat(contextWithAuthTokenMatcher),
            eq(draftPayload))).thenReturn(draftPayload);
        assertEquals(draftPayload, target.run(AUTH_TOKEN));

        verify(retrieveDraftTask, times(2)).execute(argThat(contextWithAuthTokenMatcher),eq(payload));
        verify(getInconsistentPaymentInfoTask).execute(argThat(contextWithAuthTokenMatcher),eq(casePayload));
        verify(caseDataToDivorceFormatter).execute(argThat(contextWithAuthTokenMatcher),eq(casePayload));
        verify(setCaseIdAndStateOnSession).execute(argThat(contextWithAuthTokenMatcher),eq(draftPayload));
        verify(addCourtsToPayloadTask).execute(argThat(contextWithAuthTokenMatcher),eq(draftPayload));
        verify(paymentMadeEvent).execute(argThat(contextWithAuthTokenMatcher),eq(paymentPayload));
        verify(formatDivorceSessionToCaseDataTask).execute(argThat(contextWithAuthTokenMatcher),eq(paymentPayload));
    }

}
