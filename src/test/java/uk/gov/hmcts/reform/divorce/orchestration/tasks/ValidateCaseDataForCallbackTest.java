package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidateCaseDataForCallbackTest {

    @Mock
    ValidateCaseData validateCaseData;

    @InjectMocks
    ValidateCaseDataForCallback validateCaseDataForCallback;

    private Map<String, Object> testCaseDetails;
    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testCaseDetails = new HashMap<>();
        testData = Collections.emptyMap();
        testCaseDetails.put("case_data", testData);
        context = new DefaultTaskContext();
    }

    @Test
    public void executeShouldCallCaseFormatterClientTransformToCCDFormat() throws Exception {
        when(validateCaseData.execute(context, testData)).thenReturn(testData);

        assertEquals(testCaseDetails, validateCaseDataForCallback.execute(context, testCaseDetails));

        verify(validateCaseData).execute(context, testData);
    }

    @Test(expected=TaskException.class)
    public void executeShouldThrowTaskExceptionWhenTransformToCCDFormatThrowsException() throws Exception {
        when(validateCaseData.execute(context, testData)).thenThrow(new TaskException("An Error"));

        validateCaseDataForCallback.execute(context, testCaseDetails);

        verify(validateCaseData).execute(context, testData);
    }
}