package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.DivorceCaseWrapper;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.ccd.DnCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class FormatDivorceSessionToDnCaseDataUTest {

    @Mock
    private CaseFormatterService caseFormatterService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FormatDivorceSessionToDnCaseData classUnderTest;

    @SuppressWarnings("unchecked")
    @Test
    public void whenExecute_thenProceedAsExpected() {
        final Map<String, Object> sessionData = mock(Map.class);
        final Map<String, Object> expectedOutput = mock(Map.class);

        DivorceSession divorceSession = mock(DivorceSession.class);
        DnCaseData dnCaseData = mock(DnCaseData.class);
        when(objectMapper.convertValue(sessionData, DivorceSession.class)).thenReturn(divorceSession);
        when(caseFormatterService.getDnCaseData(divorceSession)).thenReturn(dnCaseData);
        when(objectMapper.convertValue(dnCaseData, Map.class)).thenReturn(expectedOutput);

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().state(AWAITING_PRONOUNCEMENT).caseData(Collections.emptyMap()).build());
        assertEquals(expectedOutput, classUnderTest.execute(context, sessionData));

        verify(objectMapper).convertValue(sessionData, DivorceSession.class);
        verify(caseFormatterService).getDnCaseData(divorceSession);
        verify(objectMapper).convertValue(dnCaseData, Map.class);
    }

    @Test
    public void whenExecuteWithAwaitingClarificationState_thenProceedAsExpected() {
        final Map<String, Object> sessionData = mock(Map.class);
        final Map<String, Object> expectedOutput = mock(Map.class);

        DivorceSession divorceSession = mock(DivorceSession.class);
        CoreCaseData coreCaseData = mock(CoreCaseData.class);
        DnCaseData dnCaseData = mock(DnCaseData.class);
        DivorceCaseWrapper divorceCaseWrapper = new DivorceCaseWrapper(coreCaseData, divorceSession);

        when(objectMapper.convertValue(sessionData, DivorceSession.class)).thenReturn(divorceSession);
        when(objectMapper.convertValue(Collections.emptyMap(), CoreCaseData.class)).thenReturn(coreCaseData);
        when(caseFormatterService.getDnClarificationCaseData(divorceCaseWrapper)).thenReturn(dnCaseData);
        when(objectMapper.convertValue(dnCaseData, Map.class)).thenReturn(expectedOutput);

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().state(AWAITING_CLARIFICATION).caseData(Collections.emptyMap()).build());
        assertEquals(expectedOutput, classUnderTest.execute(context, sessionData));

        verify(objectMapper).convertValue(sessionData, DivorceSession.class);
        verify(objectMapper).convertValue(Collections.emptyMap(), CoreCaseData.class);
        verify(caseFormatterService).getDnClarificationCaseData(divorceCaseWrapper);
        verify(objectMapper).convertValue(dnCaseData, Map.class);
    }
}