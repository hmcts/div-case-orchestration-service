package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.ccd.AosCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormatDivorceSessionToAosCaseDataUTest {

    @Mock
    private CaseFormatterService caseFormatterClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FormatDivorceSessionToAosCaseData classUnderTest;

    @SuppressWarnings("unchecked")
    @Test
    public void whenExecute_thenProceedAsExpected() {
        final Map<String, Object> sessionData = mock(Map.class);
        final Map<String, Object> expectedOutput = mock(Map.class);

        DivorceSession divorceSession = mock(DivorceSession.class);
        AosCaseData aosCaseData = mock(AosCaseData.class);
        when(objectMapper.convertValue(sessionData, DivorceSession.class)).thenReturn(divorceSession);
        when(caseFormatterClient.getAosCaseData(divorceSession)).thenReturn(aosCaseData);
        when(objectMapper.convertValue(aosCaseData, Map.class)).thenReturn(expectedOutput);

        assertEquals(expectedOutput, classUnderTest.execute(null, sessionData));

        verify(objectMapper).convertValue(sessionData, DivorceSession.class);
        verify(caseFormatterClient).getAosCaseData(divorceSession);
        verify(objectMapper).convertValue(aosCaseData, Map.class);
    }
}