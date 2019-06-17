package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetSeparationFields;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEP_DATE;

@RunWith(MockitoJUnitRunner.class)
public class SeparationFieldsWorkflowTest {

    @Mock
    SetSeparationFields setSeparationFields;

    @InjectMocks
    SeparationFieldsWorkflow separationFieldsWorkflow;

    private Map<String, Object> testData;
    private TaskContext context;
    
    private static final String FIXED_DATE = "2019-05-11";

    @Before
    public void setup() {
        testData = Collections.emptyMap();
        context = new DefaultTaskContext();
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        Map<String, Object> resultData = Collections.singletonMap(
            D_8_REASON_FOR_DIVORCE_SEP_DATE, FIXED_DATE
        );

        when(setSeparationFields.execute(context, testData)).thenReturn(resultData);

        assertEquals(resultData, separationFieldsWorkflow.run(testData));

        verify(setSeparationFields).execute(context, testData);
    }
}
