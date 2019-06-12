package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDnGrantedDate;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateBulkCaseDnPronounceWorkflowTest {

    @Mock
    SetDnGrantedDate setDnGrantedDate;

    @InjectMocks
    UpdateBulkCaseDnPronounceWorkflow updateBulkCaseDnPronounceWorkflow;

    private TaskContext context;
    private Map<String, Object> testData;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        testData = Collections.emptyMap();
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        when(setDnGrantedDate.execute(context, testData)).thenReturn(testData);

        assertEquals(testData, updateBulkCaseDnPronounceWorkflow.run(testData));

        verify(setDnGrantedDate).execute(context, testData);
    }
}
