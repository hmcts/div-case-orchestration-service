package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataToDivorceFormatterUTest {

    @Mock
    private CaseFormatterClient caseFormatterClient;

    @InjectMocks
    private CaseDataToDivorceFormatter classUnderTest;

    @SuppressWarnings("unchecked")
    @Test
    public void whenFormatData_thenReturnExpectedData() {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        final Map<String, Object> caseData = mock(Map.class);
        final Map<String, Object> expectedResults = mock(Map.class);

        final CaseDataResponse caseDataResponseInput = CaseDataResponse.builder().data(caseData).build();

        Mockito.when(caseFormatterClient.transformToDivorceFormat(AUTH_TOKEN, caseData)).thenReturn(expectedResults);

        CaseDataResponse actual = classUnderTest.execute(context, caseDataResponseInput);

        assertEquals(expectedResults, actual.getData());

        verify(caseFormatterClient).transformToDivorceFormat(AUTH_TOKEN, caseData);
    }
}
