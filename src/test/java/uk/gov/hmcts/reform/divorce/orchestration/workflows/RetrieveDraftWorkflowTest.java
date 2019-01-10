package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataDraftToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveDraft;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetCaseIdAndStateOnSession;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPaymentInfo;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
    private GetPaymentInfo setPaymentOnSession;

    @InjectMocks
    private RetrieveDraftWorkflow target;

    @SuppressWarnings("unchecked")
    @Test
    public void givenADraft_whenExecuteSaveDraftWorkflow_thenExecuteAllTaskInOrder() throws WorkflowException {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> casePayload = mock(Map.class);
        Map<String, Object> draftPayload = mock(Map.class);

        ArgumentMatcher<TaskContext> contextWithAuthTokenMatcher =
            argument -> argument.getTransientObject(AUTH_TOKEN_JSON_KEY) != null;

        when(retrieveDraft.execute(argThat(contextWithAuthTokenMatcher), eq(payload))).thenReturn(casePayload);

        when(caseDataToDivorceFormatter.execute(argThat(contextWithAuthTokenMatcher),
                eq(casePayload))).thenReturn(draftPayload);
        when(setCaseIdAndStateOnSession.execute(argThat(contextWithAuthTokenMatcher),
                eq(draftPayload))).thenReturn(draftPayload);
        when(setPaymentOnSession.execute(argThat(contextWithAuthTokenMatcher),
                eq(draftPayload))).thenReturn(draftPayload);

        assertEquals(draftPayload, target.run(AUTH_TOKEN, true));

        verify(retrieveDraft).execute(argThat(contextWithAuthTokenMatcher),eq(payload));
        verify(caseDataToDivorceFormatter).execute(argThat(contextWithAuthTokenMatcher),eq(casePayload));
        verify(setCaseIdAndStateOnSession).execute(argThat(contextWithAuthTokenMatcher),eq(draftPayload));
        verify(setPaymentOnSession).execute(argThat(contextWithAuthTokenMatcher),eq(draftPayload));

    }

}
