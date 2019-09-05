package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_DRAFT_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataDraftToDivorceFormatterUTest {

    @Mock
    private CaseFormatterService caseFormatterService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CaseDataDraftToDivorceFormatterTask classUnderTest;

    private Map<String, Object> inputCaseData;
    private CoreCaseData coreCaseData = new CoreCaseData();

    @Before
    public void setup() {
        inputCaseData = new HashMap<>();
        DivorceSession divorceSession = new DivorceSession();
        divorceSession.setExpires(1L);

        when(objectMapper.convertValue(inputCaseData, CoreCaseData.class)).thenReturn(coreCaseData);
        when(caseFormatterService.transformToDivorceSession(coreCaseData)).thenReturn(divorceSession);
        when(objectMapper.convertValue(divorceSession, Map.class)).thenReturn(Collections.EMPTY_MAP);
    }

    @Test
    public void givenDraft_whenFormatDraftData_thenTransformToDivorceSessionWasNotCalled() {
        inputCaseData.put(IS_DRAFT_KEY, true);

        classUnderTest.execute(new DefaultTaskContext(), inputCaseData);

        verify(caseFormatterService, never()).transformToDivorceSession(any(CoreCaseData.class));
    }

    @Test
    public void givenNull_whenFormatDraftData_thenTransformToDivorceSessionWasCalled() {
        inputCaseData.put(IS_DRAFT_KEY, null);

        classUnderTest.execute(new DefaultTaskContext(), inputCaseData);

        verify(caseFormatterService, times(1)).transformToDivorceSession(any(CoreCaseData.class));
    }

    @Test
    public void givenNotDraft_whenFormatDraftData_thenTransformToDivorceSessionWasCalled() {
        inputCaseData.put(IS_DRAFT_KEY, false);

        classUnderTest.execute(new DefaultTaskContext(), inputCaseData);

        verify(caseFormatterService, times(1)).transformToDivorceSession(any(CoreCaseData.class));
    }

    @Test(expected = RuntimeException.class)
    public void givenMapperThrowsIllegalArgException_whenFormatDraftData_thenThrowRuntimeException() {
        Map<String, Object> input = new HashMap<>();
        when(objectMapper.convertValue(input, CoreCaseData.class)).thenThrow(new RuntimeException());

        classUnderTest.execute(new DefaultTaskContext(), input);
    }
}
