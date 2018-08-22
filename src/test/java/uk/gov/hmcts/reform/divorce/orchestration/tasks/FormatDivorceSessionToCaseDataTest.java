package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormatDivorceSessionToCaseDataTest {

    @Mock
    CaseFormatterClient caseFormatterClient;

    @InjectMocks
    FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    private String authToken;
    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        authToken = "authToken";
        testData = Collections.emptyMap();
        context = new DefaultTaskContext();
    }

    @Test
    public void executeShouldCallCaseFormatterClientTransformToCCDFormat() throws Exception {
        when(caseFormatterClient.transformToCCDFormat(testData, authToken)).thenReturn(testData);

        formatDivorceSessionToCaseData.setup(authToken);

        assertEquals(testData, formatDivorceSessionToCaseData.execute(context, testData));

        verify(caseFormatterClient).transformToCCDFormat(testData, authToken);
    }

    @Test(expected = TaskException.class)
    public void executeShouldThrowTaskExceptionWhenTransformToCCDFormatThrowsException() throws Exception {
        when(caseFormatterClient.transformToCCDFormat(testData, authToken)).thenThrow(new RuntimeException());

        formatDivorceSessionToCaseData.setup(authToken);

        formatDivorceSessionToCaseData.execute(context, testData);

        verify(caseFormatterClient).transformToCCDFormat(testData, authToken);
    }
}
