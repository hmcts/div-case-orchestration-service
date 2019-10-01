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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataToDivorceFormatterTaskUTest {

    @Mock
    private CaseFormatterService caseFormatterService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CaseDataToDivorceFormatterTask classUnderTest;

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
    public void whenFormatDraftData_thenTransformToDivorceSessionWasNotCalled() {
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CCD_CASE_DATA, inputCaseData);
        CaseDataResponse caseDataResponse = CaseDataResponse.builder().data(inputCaseData).build();

        classUnderTest.execute(context, caseDataResponse);

        verify(caseFormatterService, times(1)).transformToDivorceSession(any(CoreCaseData.class));
    }
}
