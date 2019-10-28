package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddCourtsToPayload;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataDraftToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetInconsistentPaymentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveDraft;
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
    private RetrieveDraft retrieveDraft;

    @Mock
    private CaseDataDraftToDivorceFormatter caseDataToDivorceFormatter;

    @Mock
    private SetCaseIdAndStateOnSession setCaseIdAndStateOnSession;

    @Mock
    private AddCourtsToPayload addCourtsToPayload;

    @Mock
    private GetInconsistentPaymentInfo getInconsistentPaymentInfo;

    @Mock
    private UpdatePaymentMadeCase paymentMadeEvent;

    @Mock
    private FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

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

        when(retrieveDraft.execute(argThat(contextWithAuthTokenMatcher), eq(payload))).thenReturn(casePayload);
        when(getInconsistentPaymentInfo.execute(argThat(contextWithAuthTokenMatcher),
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
        when(addCourtsToPayload.execute(argThat(contextWithAuthTokenMatcher),
            eq(draftPayload))).thenReturn(draftPayload);
        assertEquals(draftPayload, target.run(AUTH_TOKEN));

        verify(retrieveDraft).execute(argThat(contextWithAuthTokenMatcher),eq(payload));
        verify(getInconsistentPaymentInfo).execute(argThat(contextWithAuthTokenMatcher),eq(casePayload));
        verify(caseDataToDivorceFormatter).execute(argThat(contextWithAuthTokenMatcher),eq(casePayload));
        verify(setCaseIdAndStateOnSession).execute(argThat(contextWithAuthTokenMatcher),eq(draftPayload));
        verify(addCourtsToPayload).execute(argThat(contextWithAuthTokenMatcher),eq(draftPayload));
        verify(paymentMadeEvent, never()).execute(any(),any());
        verify(formatDivorceSessionToCaseData, never()).execute(any(),any());
    }

    @Test
    public void givenDraftWithPaymentDone_whenExecuteWorkflow_thenExecuteGetTaskInOrder() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> casePayload = mock(Map.class);
        Map<String, Object> draftPayload = mock(Map.class);
        Map<String, Object> paymentPayload = mock(Map.class);

        ArgumentMatcher<TaskContext> contextWithAuthTokenMatcher =
            argument -> argument.getTransientObject(AUTH_TOKEN_JSON_KEY) != null;

        when(retrieveDraft.execute(argThat(contextWithAuthTokenMatcher), eq(payload))).thenReturn(casePayload);
        when(getInconsistentPaymentInfo.execute(argThat(contextWithAuthTokenMatcher),
            eq(casePayload))).thenReturn(paymentPayload);
        when(formatDivorceSessionToCaseData.execute(argThat(contextWithAuthTokenMatcher),
            eq(paymentPayload))).thenReturn(paymentPayload);
        when(paymentMadeEvent.execute(argThat(contextWithAuthTokenMatcher),
            eq(paymentPayload))).thenReturn(paymentPayload);

        when(caseDataToDivorceFormatter.execute(argThat(contextWithAuthTokenMatcher),
            eq(casePayload))).thenReturn(draftPayload);
        when(setCaseIdAndStateOnSession.execute(argThat(contextWithAuthTokenMatcher),
            eq(draftPayload))).thenReturn(draftPayload);
        when(addCourtsToPayload.execute(argThat(contextWithAuthTokenMatcher),
            eq(draftPayload))).thenReturn(draftPayload);
        assertEquals(draftPayload, target.run(AUTH_TOKEN));

        verify(retrieveDraft, times(2)).execute(argThat(contextWithAuthTokenMatcher),eq(payload));
        verify(getInconsistentPaymentInfo).execute(argThat(contextWithAuthTokenMatcher),eq(casePayload));
        verify(caseDataToDivorceFormatter).execute(argThat(contextWithAuthTokenMatcher),eq(casePayload));
        verify(setCaseIdAndStateOnSession).execute(argThat(contextWithAuthTokenMatcher),eq(draftPayload));
        verify(addCourtsToPayload).execute(argThat(contextWithAuthTokenMatcher),eq(draftPayload));
        verify(paymentMadeEvent).execute(argThat(contextWithAuthTokenMatcher),eq(paymentPayload));
        verify(formatDivorceSessionToCaseData).execute(argThat(contextWithAuthTokenMatcher),eq(paymentPayload));
    }

}
