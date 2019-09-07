package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sun.jvm.hotspot.oops.ObjectHeap;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.ccd.DaCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormatDivorceSessionToDaCaseDataTaskTest {
    @Mock
    private CaseFormatterService caseFormatterService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FormatDivorceSessionToDaCaseDataTask classUnderTest;

    private Map<String, Object> input = new HashMap<>();
    private Map<String, Object> expected = new HashMap<>();

    @Before
    public void setup() {
        DaCaseData daCaseData = new DaCaseData();
        DivorceSession divorceSession = new DivorceSession();
        when(objectMapper.convertValue(input, DivorceSession.class)).thenReturn(divorceSession);
        when(caseFormatterService.getDaCaseData(divorceSession)).thenReturn(daCaseData);
        when(objectMapper.convertValue(daCaseData, Map.class)).thenReturn(expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenExecute_thenProceedAsExpected() {
        assertEquals(expected, classUnderTest.execute(null, input));
    }
}