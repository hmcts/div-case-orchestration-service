package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormatDivorceSessionToCaseDataTest {

    @Mock
    private CaseFormatterService caseFormatterService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldCallCaseFormatterClientTransformToCCDFormat() {
        final Map<String, Object> sessionData = mock(Map.class);
        final Map<String, Object> expectedOutput = mock(Map.class);

        DivorceSession divorceSession = mock(DivorceSession.class);
        CoreCaseData coreCaseData = mock(CoreCaseData.class);
        when(objectMapper.convertValue(sessionData, DivorceSession.class)).thenReturn(divorceSession);
        when(caseFormatterService.transformToCCDFormat(divorceSession, null)).thenReturn(coreCaseData);
        when(objectMapper.convertValue(coreCaseData, Map.class)).thenReturn(expectedOutput);

        assertEquals(expectedOutput, formatDivorceSessionToCaseData.execute(null, sessionData));

        verify(objectMapper).convertValue(sessionData, DivorceSession.class);
        verify(caseFormatterService).transformToCCDFormat(divorceSession, null);
        verify(objectMapper).convertValue(coreCaseData, Map.class);
    }
}