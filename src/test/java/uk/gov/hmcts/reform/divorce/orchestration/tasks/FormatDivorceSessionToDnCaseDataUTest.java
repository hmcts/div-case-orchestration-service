package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FORMATTER_CASE_DATA_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FORMATTER_DIVORCE_SESSION_KEY;

@RunWith(MockitoJUnitRunner.class)
public class FormatDivorceSessionToDnCaseDataUTest {

    @Mock
    private CaseFormatterClient caseFormatterClient;

    @InjectMocks
    private FormatDivorceSessionToDnCaseData classUnderTest;

    @SuppressWarnings("unchecked")
    @Test
    public void whenExecuteWithNotClarificationState_thenProceedAsExpected() {
        final Map<String, Object> sessionData = mock(Map.class);
        final Map<String, Object> expectedOutput = mock(Map.class);

        when(caseFormatterClient.transformToDnCaseFormat(sessionData)).thenReturn(expectedOutput);

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().state(AWAITING_DECREE_NISI).caseData(Collections.emptyMap()).build());
        assertEquals(expectedOutput, classUnderTest.execute(context, sessionData));

        verify(caseFormatterClient).transformToDnCaseFormat(sessionData);
        verify(caseFormatterClient, never()).transformToDnClarificationCaseFormat(any());
    }

    @Test
    public void whenExecuteWithAwaitingClarificationState_thenProceedAsExpected() {
        final Map<String, Object> sessionData = mock(Map.class);
        final Map<String, Object> expectedOutput = mock(Map.class);

        Map<String, Object> divorceCaseWrapper = ImmutableMap.of(
            FORMATTER_CASE_DATA_KEY, Collections.emptyMap(),
            FORMATTER_DIVORCE_SESSION_KEY, sessionData
        );

        when(caseFormatterClient.transformToDnClarificationCaseFormat(divorceCaseWrapper)).thenReturn(expectedOutput);

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().state(AWAITING_CLARIFICATION).caseData(Collections.emptyMap()).build());
        assertEquals(expectedOutput, classUnderTest.execute(context, sessionData));

        verify(caseFormatterClient, never()).transformToDnCaseFormat(any());
        verify(caseFormatterClient).transformToDnClarificationCaseFormat(divorceCaseWrapper);
    }
}