package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormatDivorceSessionToAosCaseDataUTest {

    @Mock
    private CaseFormatterClient caseFormatterClient;

    @InjectMocks
    private FormatDivorceSessionToAosCaseData classUnderTest;

    @SuppressWarnings("unchecked")
    @Test
    public void whenExecute_thenProceedAsExpected() {
        final Map<String, Object> sessionData = mock(Map.class);
        final Map<String, Object> expectedOutput = mock(Map.class);
        final DefaultTaskContext context = new DefaultTaskContext();

        when(caseFormatterClient.transformToAosCaseFormat(sessionData)).thenReturn(expectedOutput);

        assertEquals(expectedOutput, classUnderTest.execute(context, sessionData));

        verify(caseFormatterClient).transformToAosCaseFormat(sessionData);
    }
}